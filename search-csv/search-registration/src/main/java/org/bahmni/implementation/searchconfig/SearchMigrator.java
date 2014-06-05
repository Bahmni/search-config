package org.bahmni.implementation.searchconfig;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.MultiStageMigrator;
import org.bahmni.csv.StageResult;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;

import java.util.List;

public class SearchMigrator {

    private Logger logger = Logger.getLogger(SearchMigrator.class.getName());
    private OpenMRSRESTConnection openMRSRESTConnection;
    private OpenMRSRestService openMRSRestService;

    public static void main(String[] args){
        if(args.length < 6) {
            System.out.println("Incorrect command usage.");
            System.out.println(String.format("Usage %s reg-csv-file-path reg-csv-file-name hostname openmrs-user openmrs-password", SearchMigrator.class.getName()));
            System.exit(1);
        }

        String csvParentFolderPath = args[2];
        String csvFileName = args[3];
        String hostname = args[4];
        String openmrsUsername = args[5];
        String openmrsPassword = args[6];
        new SearchMigrator().process(csvParentFolderPath, csvFileName, hostname, openmrsUsername, openmrsPassword);
    }


    public void process(String csvParentFolderPath, String csvFileName, String hostname, String openmrsUsername, String openmrsPassword){
        CSVFile<SearchCSVRow> registrationCSVFile = new CSVFile<SearchCSVRow>(csvParentFolderPath, csvFileName, SearchCSVRow.class);

        getOpenmrsRestConnection(hostname, openmrsUsername, openmrsPassword);

        MultiStageMigrator multiStageMigrator = new MultiStageMigrator<SearchCSVRow>();
        multiStageMigrator.addStage(new SearchValidatorStage());
        multiStageMigrator.addStage(new KrishnaTransformerStage());
        multiStageMigrator.addStage(new PatientMigratorStage(openMRSRESTConnection, openMRSRestService));
        List<StageResult<SearchCSVRow>> migrationResult = multiStageMigrator.migrate(registrationCSVFile, SearchCSVRow.class);

        for (StageResult<SearchCSVRow> stageResult : migrationResult) {
            logger.info(stageResult.toString());
        }
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
