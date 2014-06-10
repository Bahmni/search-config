package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.SimpleStage;
import org.bahmni.csv.StageResult;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientMigratorStage implements SimpleStage<SearchCSVRow> {
    private static Logger logger = org.apache.log4j.Logger.getLogger(PatientMigratorStage.class);

    private OpenMRSRESTConnection openMRSRESTConnection;
    private OpenMRSRestService openMRSRestService;
    private final PersistenceHelper persistenceHelper;
    private final PatientPersister patientPersister;
    private final VisitPersister visitPersister;
    private boolean runInParallel = false;

    public PatientMigratorStage(OpenMRSRESTConnection openMRSRESTConnection, OpenMRSRestService openMRSRestService, boolean runInParallel) {
        this.openMRSRESTConnection = openMRSRESTConnection;
        this.openMRSRestService = openMRSRestService;
        this.runInParallel = runInParallel;

        persistenceHelper = new PersistenceHelper(openMRSRESTConnection, openMRSRestService);
        patientPersister = new PatientPersister(openMRSRESTConnection, persistenceHelper, openMRSRestService.getAllPatientAttributeTypes(), getName());
        visitPersister = new VisitPersister(openMRSRESTConnection, openMRSRestService, persistenceHelper, getName());
    }

    @Override
    public String getName() {
        return "PATIENT_MIGRATOR";
    }

    @Override
    public boolean canRunInParallel() {
        return runInParallel;
    }

    @Override
    public StageResult<SearchCSVRow> execute(List<SearchCSVRow> csvEntityList) throws MigrationException {
        ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults = new ArrayList<FailedRowResult<SearchCSVRow>>();
        JSONObject patientResponseJson;
        for (SearchCSVRow csvRow : csvEntityList) {

            if(DateUtils.isInvalidDate(csvRow.visit_date)){
                handleCreatingPatientAndVisitForInvalidDate(csvRow, failedRowResults);
            }

            else{
                if (isNewPatient(csvRow)) {
                    patientResponseJson = patientPersister.createNewPatient(csvRow, false, failedRowResults);
                    visitPersister.createVisit(patientResponseJson, failedRowResults, csvRow, false);
                } else {
                    PatientResponse patientResponse = persistenceHelper.getPatientFromOpenmrs("SEA" + csvRow.oldCaseNo);
                    if (patientResponse != null) {
                        patientResponseJson = patientPersister.updatePatient(csvRow, patientResponse, failedRowResults);
                        visitPersister.createVisit(patientResponseJson, failedRowResults, csvRow, false);
                    } else {
                        patientResponseJson = patientPersister.createNewPatient(csvRow, true, failedRowResults);
                        visitPersister.createVisit(patientResponseJson, failedRowResults, csvRow, true);
                        visitPersister.createVisit(patientResponseJson, failedRowResults, csvRow, false);
                    }
                }
            }
        }

        return new StageResult<SearchCSVRow>(getName(), failedRowResults, csvEntityList);
    }

    private void handleCreatingPatientAndVisitForInvalidDate(SearchCSVRow csvRow, ArrayList<FailedRowResult<SearchCSVRow>> failedRowResults) {
        JSONObject patientResponseJson;
        if(isNewPatient(csvRow)){
            patientResponseJson = patientPersister.createNewPatient(csvRow, false, failedRowResults);
            visitPersister.createVisitFromCaseNumber(patientResponseJson, failedRowResults, csvRow, csvRow.newCaseNo);
        }
        else{
            PatientResponse patientResponse = persistenceHelper.getPatientFromOpenmrs("SEA" + csvRow.oldCaseNo);
            if(patientResponse == null){
                patientResponseJson = patientPersister.createNewPatient(csvRow, true, failedRowResults);
                visitPersister.createVisitFromCaseNumber(patientResponseJson, failedRowResults, csvRow, csvRow.oldCaseNo);
            }
        }
    }

    private boolean isNewPatient(SearchCSVRow csvRow) {
        return StringUtils.isNotEmpty(csvRow.newCaseNo);
    }
}
