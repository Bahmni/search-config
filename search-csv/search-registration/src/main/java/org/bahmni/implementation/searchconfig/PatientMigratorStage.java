package org.bahmni.implementation.searchconfig;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.SimpleStage;
import org.bahmni.csv.StageResult;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.implementation.searchconfig.mapper.PatientRequestMapper;
import org.bahmni.implementation.searchconfig.mapper.VisitRequestMapper;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.response.PatientListResponse;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.module.bahmnicore.contract.encounter.request.BahmniEncounterTransaction;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PatientMigratorStage implements SimpleStage<SearchCSVRow> {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Logger logger = org.apache.log4j.Logger.getLogger(PatientMigratorStage.class);
    private static OpenMRSRESTConnection openMRSRESTConnection = null;
    private static String openMRSHostName = "192.168.33.10";
    private static String openmrsUserId = "superman";
    private static String openmrsUserPassword = "Admin123";
    private OpenMRSRestService openMRSRestService;
    private Gson gson = new Gson();
    private String registrationEncounterTypeUuid;
    private String opdEncounterTypeUuid;
    private String opdVisitTypeUuid;
    private String migratorProviderUuid;
    private static VisitRequestMapper visitRequestMapper;

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
            Map<String, String> allEncounterTypes = openMRSRestService.getAllEncounterTypes();
            Map<String, String> allVisitTypes = openMRSRestService.getAllVisitTypes();
            registrationEncounterTypeUuid = allEncounterTypes.get("REG");
            opdEncounterTypeUuid = allEncounterTypes.get("OPD");
            opdVisitTypeUuid = allVisitTypes.get("OPD");
            migratorProviderUuid = loadMigratorProviderUuid();
            visitRequestMapper = new VisitRequestMapper(migratorProviderUuid, opdVisitTypeUuid);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StageResult execute(List<SearchCSVRow> csvEntityList) throws MigrationException {
        JSONObject patientCreateResponse;
        ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults = new ArrayList<FailedRowResult<SearchCSVRow>>();
        for (SearchCSVRow csvRow : csvEntityList) {
            FailedRowResult<SearchCSVRow> failedRowResult = null;
            if (isNewPatient(csvRow)) {
                createNewPatient(csvRow, false, failedRowResult);
            } else {
                PatientResponse patientResponse = getPatientFromOpenmrs("SEA" + csvRow.oldCaseNo);
                if (patientResponse != null) {
                    failedRowResult = updatePatient(csvRow, patientResponse);
                } else {
                    createNewPatient(csvRow, true, failedRowResult);
                }
            }
            if (failedRowResult != null) {
                failedRowResults.add(failedRowResult);
            }
        }

        return new StageResult(getName(), failedRowResults, csvEntityList);
    }

    private FailedRowResult<SearchCSVRow> createNewPatient(SearchCSVRow csvRow, Boolean fromOldCaseNumber, FailedRowResult<SearchCSVRow> failedRowResult) {
        PatientIdentifier patientIdentifier = null;
        try {
            PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, fromOldCaseNumber);
            patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
            String patientUrl = openMRSRESTConnection.getRestApiUrl() + "patientprofile";
            JSONObject jsonResponse = postToOpenmrs(patientUrl, patientProfileRequest);
            logger.info("Created Patient: " + patientIdentifier);
            //TODO: revisit move this out of here
            createVisits(patientProfileRequest, jsonResponse, csvRow);
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

    private FailedRowResult<SearchCSVRow> createVisits(PatientProfileRequest patientProfileRequest, JSONObject jsonResponse, SearchCSVRow csvRow) {
        String patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0).getIdentifier();
        try {
            String savedPatientUuid = (String) ((JSONObject) ((JSONObject) jsonResponse.get("patient")).get("person")).get("uuid");
            Date personDateCreatedAsDate = patientProfileRequest.getPatient().getPerson().getPersonDateCreatedAsDate();

            String registrationVisitUuid = createVisit(savedPatientUuid, registrationEncounterTypeUuid, personDateCreatedAsDate);
            closeVisit(registrationVisitUuid, getVisitStopDatetimeFor(personDateCreatedAsDate));

            String opdVisitUuid = createVisit(savedPatientUuid, opdEncounterTypeUuid, personDateCreatedAsDate);
            closeVisit(opdVisitUuid, getVisitStopDatetimeFor(personDateCreatedAsDate));
        } catch (HttpServerErrorException serverErrorException) {

            logger.info(String.format("Failed to create visit for %s", patientIdentifier));
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

    private String getVisitStopDatetimeFor(Date visitStartDateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(visitStartDateTime);
        cal.add(Calendar.HOUR, 4);
        return DateUtils.stringify(cal.getTime());
    }

    private void closeVisit(String visitUuid, String stopDatetime) throws IOException, ParseException {
        String url = openMRSRESTConnection.getRestApiUrl() + "visit/" + visitUuid;
        String requestString = "{\n" +
                "  \"stopDatetime\":\"%s\"\n" +
                "}";
        postToOpenmrs(url, String.format(requestString, stopDatetime));
    }

    private String createVisit(String savedPatientUuid, String encounterTypeUuid, Date visitDateTime) throws java.text.ParseException, IOException, ParseException {
        BahmniEncounterTransaction bahmniEncounterTransaction = visitRequestMapper.mapVisitRequest(savedPatientUuid, encounterTypeUuid, visitDateTime);
        JSONObject jsonResponse = postToOpenmrs(getEncounterTransactionUrl(), bahmniEncounterTransaction);
        return (String) jsonResponse.get("visitUuid");
    }

    private String getEncounterTransactionUrl() {
        return String.format("http://%s:8080/openmrs/ws/rest/emrapi/encounter", openMRSRESTConnection.getServer());
    }

    private boolean isNewPatient(SearchCSVRow csvRow) {
        return StringUtils.isNotEmpty(csvRow.newCaseNo);
    }

    private FailedRowResult<SearchCSVRow> updatePatient(SearchCSVRow csvRow, PatientResponse patientResponse) {
        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatientForUpdate(csvRow, patientResponse);
        PatientIdentifier patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
        try {
            String patientUuid = patientResponse.getUuid();
            String patientUpdateUrl = openMRSRESTConnection.getRestApiUrl() + "patientprofile/" + patientUuid;
            postToOpenmrs(patientUpdateUrl, patientProfileRequest);
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

    private String loadMigratorProviderUuid() throws ParseException {
        String url = openMRSRESTConnection.getRestApiUrl() + "provider?v=custom:(uuid,identifier)&q=MIGRATOR";
        ResponseEntity<String> response = getFromOpenmrs(url);
        JSONObject parsedResponse = (JSONObject) new JSONParser().parse(response.getBody());
        List<Map<String, String>> results = (List<Map<String, String>>) parsedResponse.get("results");
        return results.get(0).get("uuid");
    }

    private PatientResponse getPatientFromOpenmrs(String patientIdentifier) {
        String representation = "custom:(uuid,person:(uuid,preferredAddress:(uuid,preferred),preferredName:(uuid)))";
        String url = openMRSRESTConnection.getRestApiUrl() + "patient?q=" + patientIdentifier + "&v=" + representation;
        ResponseEntity<String> response = getFromOpenmrs(url);
        PatientListResponse patientListResponse = gson.fromJson(response.getBody(), PatientListResponse.class);
        if (patientListResponse.getResults().size() > 0) {
            return patientListResponse.getResults().get(0);
        }
        return null;
    }

    private ResponseEntity<String> getFromOpenmrs(String url) {
        HttpEntity httpEntity = new HttpEntity(getHttpHeaders());
        return new RestTemplate().exchange(url, HttpMethod.GET, httpEntity, String.class);
    }

    private JSONObject postToOpenmrs(String url, Object request) throws IOException, ParseException {
        String jsonRequest = objectMapper.writeValueAsString(request);
        if (logger.isDebugEnabled()) logger.debug(jsonRequest);
        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(request, httpHeaders);
        ResponseEntity<String> out = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
        if (logger.isDebugEnabled()) logger.debug(out.getBody());
        JSONObject parsedResponse = (JSONObject) new JSONParser().parse(out.getBody());
        return parsedResponse;
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
