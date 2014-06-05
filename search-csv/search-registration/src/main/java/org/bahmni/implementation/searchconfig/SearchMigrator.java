package org.bahmni.implementation.searchconfig;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.MultiStageMigrator;
import org.bahmni.csv.StageResult;

import java.util.List;

public class SearchMigrator {

    private Logger logger = Logger.getLogger(SearchMigrator.class.getName());

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

        MultiStageMigrator multiStageMigrator = new MultiStageMigrator<SearchCSVRow>();
        multiStageMigrator.addStage(new SearchValidatorStage());
        multiStageMigrator.addStage(new KrishnaTransformerStage());
        multiStageMigrator.addStage(new PatientMigratorStage(hostname, openmrsUsername, openmrsPassword));
        List<StageResult<SearchCSVRow>> migrationResult = multiStageMigrator.migrate(registrationCSVFile, SearchCSVRow.class);

        for (StageResult<SearchCSVRow> stageResult : migrationResult) {
            logger.info(stageResult.toString());
        }
    }

}
