package org.bahmni.implementation.searchconfig;

import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.SimpleStage;
import org.bahmni.csv.StageResult;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
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
    private static String openmrsUserId = "admin";
    private static String openmrsUserPassword = "test";
    private OpenMRSRestService openMRSRestService;


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
            PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapFrom(csvRow);
            PatientIdentifier  patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
            String jsonRequest = "";
            try {
                jsonRequest = objectMapper.writeValueAsString(patientProfileRequest);
                System.out.println(jsonRequest);

                if (logger.isDebugEnabled()) logger.debug(jsonRequest);
                HttpHeaders httpHeaders = getHttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity entity = new HttpEntity(patientProfileRequest, httpHeaders);
                String url = openMRSRESTConnection.getRestApiUrl() + "patientprofile";
                ResponseEntity<String> out = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
                if (logger.isDebugEnabled()) logger.debug(out.getBody());
                logger.info(String.format("Successfully created %s", patientIdentifier));
            } catch (HttpServerErrorException serverErrorException) {
                logger.info(String.format("Failed to create %s", patientIdentifier));
                logger.info("Patient request: " + jsonRequest);
                logger.error("Patient create response: " + serverErrorException.getResponseBodyAsString());
                String errorMessage = extractErrorMessage(serverErrorException);
                failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, getName() + ":" + errorMessage));
            } catch (Exception e) {
                logger.info(String.format("Failed to create"));
                logger.info("Patient request: " + jsonRequest);
                logger.error("Failed to process a patient", e);
                failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
            }
        }

        return new StageResult(getName(), failedRowResults, csvEntityList);
    }

    private String extractErrorMessage(HttpServerErrorException serverErrorException) {
        String responseBody = serverErrorException.getResponseBodyAsString();
        int startIndex = responseBody.indexOf("message");
        int endIndex = responseBody.indexOf(",\"code\"");
        String message = responseBody.substring(startIndex,endIndex);
        //Replacing quotes since the error message will be written to a csv
        return message.replaceAll("\"", "");
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Cookie", "JSESSIONID=" + openMRSRestService.getSessionId());
        return requestHeaders;
    }

}
