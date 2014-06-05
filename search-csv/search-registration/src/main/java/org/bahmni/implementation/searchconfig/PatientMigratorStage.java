package org.bahmni.implementation.searchconfig;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.SimpleStage;
import org.bahmni.csv.StageResult;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.implementation.searchconfig.mapper.DateMapper;
import org.bahmni.implementation.searchconfig.mapper.PatientRequestMapper;
import org.bahmni.implementation.searchconfig.mapper.VisitRequestMapper;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.response.PatientListResponse;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.module.bahmnicore.contract.encounter.request.BahmniEncounterTransaction;
import org.bahmni.openmrsconnector.AllPatientAttributeTypes;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PatientMigratorStage implements SimpleStage<SearchCSVRow> {
    private static Logger logger = org.apache.log4j.Logger.getLogger(PatientMigratorStage.class);

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static OpenMRSRESTConnection openMRSRESTConnection = null;
    private static String migratorProviderUuid = null;
    private static String registrationFeeConceptUuid = null;

    private VisitRequestMapper visitRequestMapper;
    private String registrationEncounterTypeUuid;
    private String opdEncounterTypeUuid;
    private OpenMRSRestService openMRSRestService;
    private AllPatientAttributeTypes allPatientAttributeTypes;

    public PatientMigratorStage(OpenMRSRESTConnection openMRSRESTConnection, OpenMRSRestService openMRSRestService) {
        this.openMRSRESTConnection = openMRSRESTConnection;
        this.openMRSRestService = openMRSRestService;
        getResources();
    }

    private void getResources() {
        try {
            Map<String, String> allEncounterTypes = openMRSRestService.getAllEncounterTypes();
            allPatientAttributeTypes = openMRSRestService.getAllPatientAttributeTypes();
            Map<String, String> allVisitTypes = openMRSRestService.getAllVisitTypes();
            registrationEncounterTypeUuid = allEncounterTypes.get("REG");
            opdEncounterTypeUuid = allEncounterTypes.get("OPD");
            String opdVisitTypeUuid = allVisitTypes.get("OPD");

            migratorProviderUuid = getMigratorProviderUuid();
            registrationFeeConceptUuid = getRegistrationFeeConcept();
            visitRequestMapper = new VisitRequestMapper(migratorProviderUuid, opdVisitTypeUuid, registrationEncounterTypeUuid, registrationFeeConceptUuid);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "PATIENT_MIGRATOR";
    }

    @Override
    public boolean canRunInParallel() {
        return true;
    }

    @Override
    public StageResult execute(List<SearchCSVRow> csvEntityList) throws MigrationException {
        ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults = new ArrayList<FailedRowResult<SearchCSVRow>>();
        JSONObject patientResponseJson;
        for (SearchCSVRow csvRow : csvEntityList) {
            Date visitDate = DateMapper.getDateFromVisitDate(csvRow);

            if (isNewPatient(csvRow)) {
                patientResponseJson = createNewPatient(csvRow, false, failedRowResults);
                //create visit with reg and opd encounter for visit_date
                createVisit(patientResponseJson, visitDate, failedRowResults, csvRow);
            } else {
                PatientResponse patientResponse = getPatientFromOpenmrs("SEA" + csvRow.oldCaseNo);
                if (patientResponse != null) {
                    patientResponseJson = updatePatient(csvRow, patientResponse, failedRowResults);
                    //create visit with opd and reg encounter for visit_date
                    createVisit(patientResponseJson, visitDate, failedRowResults, csvRow);
                } else {
                    patientResponseJson = createNewPatient(csvRow, true, failedRowResults);
                    //create visit with reg and opd encounter for patient created date
                    createVisit(patientResponseJson, DateMapper.getDateFromOldCaseNumber(csvRow), failedRowResults, csvRow);
                    //create opd and reg encounter for visit_date
                    createVisit(patientResponseJson, visitDate, failedRowResults, csvRow);
                }
            }
        }

        return new StageResult(getName(), failedRowResults, csvEntityList);
    }

    private void createVisit(JSONObject patientResponse, Date visitDate, List failedRowResults, SearchCSVRow csvRow) {
        List<String> encounterTypeUuids = Arrays.asList(opdEncounterTypeUuid, registrationEncounterTypeUuid);
        if (patientResponse == null) {
            return;
        }
        JSONObject patient = (JSONObject) patientResponse.get("patient");
        String patientIdentifier = (String) ((ArrayList<JSONObject>) patient.get("identifiers")).get(0).get("identifier");
        JSONObject person = (JSONObject) patient.get("person");
        String savedPatientUuid = (String) person.get("uuid");
        try {
            String visitUuid = "";
            for (String encounterTypeUuid : encounterTypeUuids) {
                BahmniEncounterTransaction bahmniEncounterTransaction = visitRequestMapper.mapVisitRequest(savedPatientUuid, encounterTypeUuid, visitDate, csvRow);
                JSONObject visitResponse = postToOpenmrs(getEncounterTransactionUrl(), bahmniEncounterTransaction);
                visitUuid = (String) visitResponse.get("visitUuid");
            }
            //Assuming Bahmni ET will update the active encounter the second time.
            closeVisit(visitUuid, getVisitStopDatetimeFor(visitDate));
            logger.info("Created Visit for patient: " + patientIdentifier);
        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to create visit for %s", patientIdentifier));
            logger.error("Visit create response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = extractErrorMessage(serverErrorException);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, getName() + ":" + errorMessage));
        } catch (Exception e) {
            logger.info(String.format("Failed to create visit for %s", patientIdentifier));
            logger.error("Failed to process a visit", e);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
        }
    }

    private JSONObject createNewPatient(SearchCSVRow csvRow, Boolean fromOldCaseNumber, ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults) {
        PatientIdentifier patientIdentifier = null;
        try {
            PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, fromOldCaseNumber, allPatientAttributeTypes);
            patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
            String patientUrl = openMRSRESTConnection.getRestApiUrl() + "patientprofile";
            JSONObject jsonResponse = postToOpenmrs(patientUrl, patientProfileRequest);
            logger.info("Created Patient: " + patientIdentifier);
            return jsonResponse;

        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to create %s", patientIdentifier));
            logger.error("Patient create response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = extractErrorMessage(serverErrorException);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, getName() + ":" + errorMessage));
        } catch (Exception e) {
            logger.info(String.format("Failed to create %s", patientIdentifier));
            logger.error("Failed to process a patient", e);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
        }
        return null;
    }

    private JSONObject updatePatient(SearchCSVRow csvRow, PatientResponse patientResponse, ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults) {
        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatientForUpdate(csvRow, patientResponse, allPatientAttributeTypes);
        PatientIdentifier patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
        try {
            String patientUuid = patientResponse.getUuid();
            String patientUpdateUrl = openMRSRESTConnection.getRestApiUrl() + "patientprofile/" + patientUuid;
            JSONObject jsonObject = postToOpenmrs(patientUpdateUrl, patientProfileRequest);
            logger.info("Updating Patient: " + patientIdentifier);
            return jsonObject;
        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to update %s", patientIdentifier));
            logger.error("Patient update response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = extractErrorMessage(serverErrorException);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, getName() + ":" + errorMessage));
        } catch (Exception e) {
            logger.info(String.format("Failed to update %s", patientIdentifier));
            logger.error("Failed to process a patient", e);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
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

    private String getEncounterTransactionUrl() {
        return String.format("http://%s:8080/openmrs/ws/rest/emrapi/encounter", openMRSRESTConnection.getServer());
    }

    private boolean isNewPatient(SearchCSVRow csvRow) {
        return StringUtils.isNotEmpty(csvRow.newCaseNo);
    }

    private String getMigratorProviderUuid() throws ParseException {
        if(migratorProviderUuid == null){
            String url = openMRSRESTConnection.getRestApiUrl() + "provider?v=custom:(uuid,identifier)&q=MIGRATOR";
            ResponseEntity<String> response = getFromOpenmrs(url);
            JSONObject parsedResponse = (JSONObject) new JSONParser().parse(response.getBody());
            List<Map<String, String>> results = (List<Map<String, String>>) parsedResponse.get("results");
            return results.get(0).get("uuid");
        }
        return migratorProviderUuid;
    }

    private String getRegistrationFeeConcept() throws ParseException {
        if(registrationFeeConceptUuid == null){
            String url = openMRSRESTConnection.getRestApiUrl() + "concept?q='REGISTRATION FEES'";
            ResponseEntity<String> response = getFromOpenmrs(url);
            JSONObject parsedResponse = (JSONObject) new JSONParser().parse(response.getBody());
            List<Map<String, String>> results = (List<Map<String, String>>) parsedResponse.get("results");
            return results.get(0).get("uuid");
        }
        return registrationFeeConceptUuid;
    }

    private PatientResponse getPatientFromOpenmrs(String patientIdentifier) {
        String representation = "custom:(uuid,person:(uuid,preferredAddress:(uuid,preferred),preferredName:(uuid)))";
        String url = openMRSRESTConnection.getRestApiUrl() + "patient?q=" + patientIdentifier + "&v=" + representation;
        ResponseEntity<String> response = getFromOpenmrs(url);
        PatientListResponse patientListResponse = new Gson().fromJson(response.getBody(), PatientListResponse.class);
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
