package org.bahmni.implementation.searchconfig;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.MultiStageMigrator;
import org.bahmni.csv.StageResult;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;

import java.util.Date;
import java.util.List;

public class SearchMigrator {

    private Logger logger = Logger.getLogger(SearchMigrator.class.getName());
    private OpenMRSRESTConnection openMRSRESTConnection;
    private OpenMRSRestService openMRSRestService;

    public static void main(String[] args){
        if(args.length < 6) {
            System.out.println("Incorrect command usage.");
            System.out.println(String.format("Usage %s reg-csv-file-path reg-csv-file-name hostname openmrs-user openmrs-password runInParallel[true|false]", SearchMigrator.class.getName()));
            System.exit(1);
        }

        String csvParentFolderPath = args[0];
        String csvFileName = args[1];
        String hostname = args[2];
        String openmrsUsername = args[3];
        String openmrsPassword = args[4];
        String runMigratorInParallel = args[5];
        new SearchMigrator().process(csvParentFolderPath, csvFileName, hostname, openmrsUsername, openmrsPassword, runMigratorInParallel);
        System.exit(0);
    }


    public void process(String csvParentFolderPath, String csvFileName, String hostname, String openmrsUsername, String openmrsPassword, String runMigratorInParallel){
        Date startTime = new Date();
        CSVFile<SearchCSVRow> registrationCSVFile = new CSVFile<SearchCSVRow>(csvParentFolderPath, csvFileName);

        getOpenmrsRestConnection(hostname, openmrsUsername, openmrsPassword);

        MultiStageMigrator multiStageMigrator = new MultiStageMigrator<SearchCSVRow>();
        multiStageMigrator.addStage(new SearchValidatorStage());
        multiStageMigrator.addStage(new KrishnaTransformerStage());
        multiStageMigrator.addStage(new PatientMigratorStage(openMRSRESTConnection, openMRSRestService, Boolean.parseBoolean(runMigratorInParallel)));
        List<StageResult<SearchCSVRow>> migrationResult = multiStageMigrator.migrate(registrationCSVFile, SearchCSVRow.class);
        Date endTime = new Date();

        for (StageResult<SearchCSVRow> stageResult : migrationResult) {
            logger.info(stageResult.toString());
        }
        logger.info("Time taken : " + DateUtils.getDiff(startTime, endTime));
    }

    private void getOpenmrsRestConnection(String openMRSHostName, String openmrsUserId, String openmrsUserPassword) {
        openMRSRESTConnection = new OpenMRSRESTConnection(openMRSHostName, openmrsUserId, openmrsUserPassword);
        try {
            openMRSRestService = new OpenMRSRestService(openMRSRESTConnection);
        } catch (Exception e) {
            logger.error("Could not authenticate", e);
        }
    }

}
