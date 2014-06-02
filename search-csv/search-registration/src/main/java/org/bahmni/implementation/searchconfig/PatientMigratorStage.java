package org.bahmni.implementation.searchconfig;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.SimpleStage;
import org.bahmni.csv.StageResult;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.response.PatientListResponse;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class PatientMigratorStage implements SimpleStage<SearchCSVRow> {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Logger logger = org.apache.log4j.Logger.getLogger(PatientMigratorStage.class);
    private static OpenMRSRESTConnection openMRSRESTConnection = null;
    private static String openMRSHostName = "192.168.33.10";
    private static String openmrsUserId = "superman";
    private static String openmrsUserPassword = "Admin123";
    private OpenMRSRestService openMRSRestService;
    private Gson gson = new Gson();


    @Override
    public String getName() {
        return "PATIENT_MIGRATOR";
    }

    @Override
    public boolean canRunInParallel() {
        return true;
    }

    {
        try {
            openMRSRESTConnection = new OpenMRSRESTConnection(openMRSHostName, openmrsUserId, openmrsUserPassword);
            openMRSRestService = new OpenMRSRestService(openMRSRESTConnection);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StageResult execute(List<SearchCSVRow> csvEntityList) throws MigrationException {
        ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults = new ArrayList<FailedRowResult<SearchCSVRow>>();
        for (SearchCSVRow csvRow : csvEntityList) {
            FailedRowResult<SearchCSVRow> failedRowResult;
            if (isNewPatient(csvRow)) {
                failedRowResult = createNewPatient(csvRow, false);
            } else {
                PatientResponse patientResponse = getPatientFromOpenmrs("SEA" + csvRow.oldCaseNo);
                if (patientResponse != null) {
                    failedRowResult = updatePatient(csvRow, patientResponse);
                } else {
                    failedRowResult = createNewPatient(csvRow, true);
                }
            }
            if (failedRowResult != null) {
                failedRowResults.add(failedRowResult);
            }
        }

        return new StageResult(getName(), failedRowResults, csvEntityList);
    }

    private boolean isNewPatient(SearchCSVRow csvRow) {
        return StringUtils.isNotEmpty(csvRow.newCaseNo);
    }

    private FailedRowResult<SearchCSVRow> createNewPatient(SearchCSVRow csvRow, Boolean fromOldCaseNumber) {
        PatientIdentifier patientIdentifier = null;
        try {
            PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, fromOldCaseNumber);
            patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
            postToOpenmrs("patientprofile", patientProfileRequest);
            logger.info("Creating Patient: " + patientIdentifier);
        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to create %s", patientIdentifier));
            logger.error("Patient create response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = extractErrorMessage(serverErrorException);
            return new FailedRowResult<SearchCSVRow>(csvRow, getName() + ":" + errorMessage);
        } catch (Exception e) {
            logger.info(String.format("Failed to create %s", patientIdentifier));
            logger.error("Failed to process a patient", e);
            return new FailedRowResult<SearchCSVRow>(csvRow, e);
        }
        return null;
    }

    private FailedRowResult<SearchCSVRow> updatePatient(SearchCSVRow csvRow, PatientResponse patientResponse) {
        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatientForUpdate(csvRow, patientResponse);
        PatientIdentifier patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
        try {
            String patientUuid = patientResponse.getUuid();
            postToOpenmrs("patientprofile/" + patientUuid, patientProfileRequest);
            logger.info("Updating Patient: " + patientIdentifier);
        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to update %s", patientIdentifier));
            logger.error("Patient update response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = extractErrorMessage(serverErrorException);
            return new FailedRowResult<SearchCSVRow>(csvRow, getName() + ":" + errorMessage);
        } catch (Exception e) {
            logger.info(String.format("Failed to update %s", patientIdentifier));
            logger.error("Failed to process a patient", e);
            return new FailedRowResult<SearchCSVRow>(csvRow, e);
        }
        return null;
    }

    private PatientResponse getPatientFromOpenmrs(String patientIdentifier) {
        String representation = "custom:(uuid,person:(uuid,preferredAddress:(uuid,preferred),preferredName:(uuid)))";
        String url = openMRSRESTConnection.getRestApiUrl() + "patient?q=" + patientIdentifier + "&v=" + representation;
        HttpEntity httpEntity = new HttpEntity(getHttpHeaders());
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, httpEntity, String.class);
        PatientListResponse patientListResponse = gson.fromJson(response.getBody(), PatientListResponse.class);
        if (patientListResponse.getResults().size() > 0) {
            return patientListResponse.getResults().get(0);
        }
        return null;
    }

    private void postToOpenmrs(String openmrsEndpoint, Object request) throws IOException {
        String jsonRequest = objectMapper.writeValueAsString(request);
        if (logger.isDebugEnabled()) logger.debug(jsonRequest);
        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(request, httpHeaders);
        String url = openMRSRESTConnection.getRestApiUrl() + openmrsEndpoint;
        ResponseEntity<String> out = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
        if (logger.isDebugEnabled()) logger.debug(out.getBody());
    }

    private String extractErrorMessage(HttpServerErrorException serverErrorException) {
        String responseBody = serverErrorException.getResponseBodyAsString();
        int startIndex = responseBody.indexOf("message");
        int endIndex = responseBody.indexOf(",\"code\"");
        String message = responseBody.substring(startIndex, endIndex);
        //Replacing quotes since the error message will be written to a csv
        return message.replaceAll("\"", "");
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Cookie", "JSESSIONID=" + openMRSRestService.getSessionId());
        return requestHeaders;
    }

}
