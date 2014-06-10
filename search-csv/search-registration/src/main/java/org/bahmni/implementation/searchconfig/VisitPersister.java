package org.bahmni.implementation.searchconfig;

import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.implementation.searchconfig.mapper.DateMapper;
import org.bahmni.implementation.searchconfig.mapper.VisitRequestMapper;
import org.bahmni.module.bahmnicore.contract.encounter.request.BahmniEncounterTransaction;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class VisitPersister {
    private static Logger logger = org.apache.log4j.Logger.getLogger(VisitPersister.class);

    private OpenMRSRESTConnection openMRSRESTConnection;
    private OpenMRSRestService openMRSRestService;
    private PersistenceHelper persistenceHelper;
    private VisitRequestMapper visitRequestMapper;
    private String stageName;

    private String opdEncounterTypeUuid;
    private String registrationEncounterTypeUuid;
    private String migratorProviderUuid;
    private String registrationFeeConceptUuid;
    private List<String> encounterTypeUuids;

    public VisitPersister(OpenMRSRESTConnection openMRSRESTConnection, OpenMRSRestService openMRSRestService, PersistenceHelper persistenceHelper, String stageName) {
        this.openMRSRESTConnection = openMRSRESTConnection;
        this.persistenceHelper = persistenceHelper;
        this.stageName = stageName;
        this.openMRSRestService = openMRSRestService;
        initializeResources();
    }

    private void initializeResources() {
        try{
            Map<String, String> allEncounterTypes = openMRSRestService.getAllEncounterTypes();
            Map<String, String> allVisitTypes = openMRSRestService.getAllVisitTypes();
            registrationEncounterTypeUuid = allEncounterTypes.get("REG");
            opdEncounterTypeUuid = allEncounterTypes.get("OPD");
            String opdVisitTypeUuid = allVisitTypes.get("OPD");

            String migratorProviderUuid = getMigratorProviderUuid();
            String registrationFeeConceptUuid = getRegistrationFeeConcept();
            encounterTypeUuids = Arrays.asList(opdEncounterTypeUuid, registrationEncounterTypeUuid);
            visitRequestMapper = new VisitRequestMapper(migratorProviderUuid, opdVisitTypeUuid, registrationEncounterTypeUuid, registrationFeeConceptUuid);
        } catch (Exception e) {
            logger.error("Could not get visit resources", e);
        }

    }

    public void createVisit(JSONObject patientResponse, List<FailedRowResult<SearchCSVRow>> failedRowResults,
                            SearchCSVRow csvRow, boolean fromOldCaseNumber) {
        if (patientResponse == null){
            logger.debug("Not creating visit since patient was not created");
            return;
        }

        JSONObject patient = (JSONObject) patientResponse.get("patient");
        String patientIdentifier = (String) ((ArrayList<JSONObject>) patient.get("identifiers")).get(0).get("identifier");
        String savedPatientUuid = (String) ((JSONObject) patient.get("person")).get("uuid");

        try {

            String visitUuid = "";
            Date visitDate = DateMapper.getVisitDate(csvRow, fromOldCaseNumber);

            for (String encounterTypeUuid : encounterTypeUuids) {
                BahmniEncounterTransaction bahmniEncounterTransaction =
                        visitRequestMapper.mapVisitRequest(savedPatientUuid, encounterTypeUuid, visitDate, csvRow, fromOldCaseNumber);
                JSONObject visitResponse = persistenceHelper.postToOpenmrs(getEncounterTransactionUrl(), bahmniEncounterTransaction);
                visitUuid = (String) visitResponse.get("visitUuid");
            }

            //Assuming Bahmni ET will update the active encounter the second time.
            closeVisit(visitUuid, DateMapper.getVisitStopDatetimeFor(visitDate));
            logger.info("Created Visit for patient: " + patientIdentifier);
        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to create visit for %s", patientIdentifier));
            logger.error("Visit create response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = persistenceHelper.extractErrorMessage(serverErrorException);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, stageName + ":" + errorMessage));
        } catch (Exception e) {
            logger.info(String.format("Failed to create visit for %s", patientIdentifier));
            logger.error("Failed to process a visit", e);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
        }
    }

    public void createVisitFromCaseNumber(JSONObject patientResponse, ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults, SearchCSVRow csvRow, String caseNo) {
        if (patientResponse == null){
            logger.debug("Not creating visit since patient was not created");
            return;
        }

        JSONObject patient = (JSONObject) patientResponse.get("patient");
        String patientIdentifier = (String) ((ArrayList<JSONObject>) patient.get("identifiers")).get(0).get("identifier");
        String savedPatientUuid = (String) ((JSONObject) patient.get("person")).get("uuid");

        try {
            String visitUuid = "";
            Date visitDate = DateMapper.getVisitDateFromCaseNumber(caseNo);
            for (String encounterTypeUuid : encounterTypeUuids) {
                BahmniEncounterTransaction bahmniEncounterTransaction =
                        visitRequestMapper.mapVisitRequest(savedPatientUuid, encounterTypeUuid, visitDate, csvRow, false);
                JSONObject visitResponse = persistenceHelper.postToOpenmrs(getEncounterTransactionUrl(), bahmniEncounterTransaction);
                visitUuid = (String) visitResponse.get("visitUuid");
            }
            closeVisit(visitUuid, DateMapper.getVisitStopDatetimeFor(visitDate));
            logger.info("Created Visit for patient: " + patientIdentifier);
        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to create visit for %s", patientIdentifier));
            logger.error("Visit create response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = persistenceHelper.extractErrorMessage(serverErrorException);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, stageName + ":" + errorMessage));
        } catch (Exception e) {
            logger.info(String.format("Failed to create visit for %s", patientIdentifier));
            logger.error("Failed to process a visit", e);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
        }
    }

    private void closeVisit(String visitUuid, String stopDatetime) throws IOException, ParseException {
        String url = openMRSRESTConnection.getRestApiUrl() + "visit/" + visitUuid;
        String requestString = "{\n" +
                "  \"stopDatetime\":\"%s\"\n" +
                "}";
        persistenceHelper.postToOpenmrs(url, String.format(requestString, stopDatetime));
    }

    private String getEncounterTransactionUrl() {
        return String.format("http://%s:8080/openmrs/ws/rest/emrapi/encounter", openMRSRESTConnection.getServer());
    }

    private String getMigratorProviderUuid() throws ParseException {
        if(migratorProviderUuid == null){
            String url = openMRSRESTConnection.getRestApiUrl() + "provider?v=custom:(uuid,identifier)&q=MIGRATOR";
            ResponseEntity<String> response = persistenceHelper.getFromOpenmrs(url);
            JSONObject parsedResponse = (JSONObject) new JSONParser().parse(response.getBody());
            List<Map<String, String>> results = (List<Map<String, String>>) parsedResponse.get("results");
            migratorProviderUuid = results.get(0).get("uuid");
        }
        return migratorProviderUuid;
    }

    private String getRegistrationFeeConcept() throws ParseException {
        if(registrationFeeConceptUuid == null){
            String url = openMRSRESTConnection.getRestApiUrl() + "concept?q='REGISTRATION FEES'";
            ResponseEntity<String> response = persistenceHelper.getFromOpenmrs(url);
            JSONObject parsedResponse = (JSONObject) new JSONParser().parse(response.getBody());
            List<Map<String, String>> results = (List<Map<String, String>>) parsedResponse.get("results");
            registrationFeeConceptUuid = results.get(0).get("uuid");
        }
        return registrationFeeConceptUuid;
    }
}
