package org.bahmni.implementation.searchconfig;

import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.implementation.searchconfig.mapper.PatientRequestMapper;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.openmrsconnector.AllPatientAttributeTypes;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.json.simple.JSONObject;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class PatientPersister {
    private static Logger logger = org.apache.log4j.Logger.getLogger(PatientPersister.class);
    private static Properties TAHSIL_TO_DISTRICT = new Properties();

    private OpenMRSRESTConnection openMRSRESTConnection;
    private PersistenceHelper persistenceHelper;
    private AllPatientAttributeTypes allPatientAttributeTypes;
    private String stageName;

    public PatientPersister(OpenMRSRESTConnection openMRSRESTConnection, PersistenceHelper persistenceHelper, AllPatientAttributeTypes allPatientAttributeTypes, String stageName) {
        this.openMRSRESTConnection = openMRSRESTConnection;
        this.persistenceHelper = persistenceHelper;
        this.allPatientAttributeTypes = allPatientAttributeTypes;
        this.stageName = stageName;
        initializeTahsilToDistrictMappingFromProperties();
    }

    public JSONObject createNewPatient(SearchCSVRow csvRow, Boolean fromOldCaseNumber, ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults) {
        PatientIdentifier patientIdentifier = null;
        try {
            PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, fromOldCaseNumber, allPatientAttributeTypes, TAHSIL_TO_DISTRICT);
            patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
            String patientUrl = openMRSRESTConnection.getRestApiUrl() + "patientprofile";
            JSONObject jsonResponse = persistenceHelper.postToOpenmrs(patientUrl, patientProfileRequest);
            logger.info("Created Patient: " + patientIdentifier);
            return jsonResponse;

        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to create %s", patientIdentifier));
            logger.error("Patient create response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = persistenceHelper.extractErrorMessage(serverErrorException);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, stageName + ":" + errorMessage));
        } catch (Exception e) {
            logger.info(String.format("Failed to create %s", patientIdentifier));
            logger.error("Failed to process a patient", e);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
        }
        return null;
    }

    public JSONObject updatePatient(SearchCSVRow csvRow, PatientResponse patientResponse, ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults) {
        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatientForUpdate(csvRow, patientResponse, allPatientAttributeTypes, TAHSIL_TO_DISTRICT);
        PatientIdentifier patientIdentifier = patientProfileRequest.getPatient().getIdentifiers().get(0);
        try {
            String patientUuid = patientResponse.getUuid();
            String patientUpdateUrl = openMRSRESTConnection.getRestApiUrl() + "patientprofile/" + patientUuid;
            JSONObject jsonObject = persistenceHelper.postToOpenmrs(patientUpdateUrl, patientProfileRequest);
            logger.info("Updating Patient: " + patientIdentifier);
            return jsonObject;
        } catch (HttpServerErrorException serverErrorException) {
            logger.info(String.format("Failed to update %s", patientIdentifier));
            logger.error("Patient update response: " + serverErrorException.getResponseBodyAsString());
            String errorMessage = persistenceHelper.extractErrorMessage(serverErrorException);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, stageName + ":" + errorMessage));
        } catch (Exception e) {
            logger.info(String.format("Failed to update %s", patientIdentifier));
            logger.error("Failed to process a patient", e);
            failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e));
        }
        return null;
    }

    private void initializeTahsilToDistrictMappingFromProperties() {
        try {
            TAHSIL_TO_DISTRICT.load(getClass().getClassLoader().getResourceAsStream("tahsilDistrictMapping.properties"));
        } catch (IOException e) {
            logger.error("Could not load tahsil to district mapping file.", e);
        }
    }
}
