package org.bahmni.implementation.searchconfig;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.MigrateResult;
import org.bahmni.csv.MigratorBuilder;
import org.bahmni.csv.exception.MigrationException;

import java.io.IOException;

public class SearchMigrator {

    private Logger logger = Logger.getLogger(SearchMigrator.class.getName());

    public static void main(String[] args){
        if(args.length < 2) {
            System.out.println("Incorrect command usage.");
            System.out.println(String.format("Usage %s [validate|migrate] reg-csv-file-path", SearchMigrator.class.getName()));
            System.exit(1);
        }

        String csvParentFolderPath = args[2];
        String csvFileName = args[3];
        new SearchMigrator().process(csvParentFolderPath, csvFileName);
    }

    public void process(String csvParentFolderPath, String csvFileName){
        org.bahmni.csv.Migrator migrator = new MigratorBuilder(SearchCSVRow.class)
                .readFrom(csvParentFolderPath, csvFileName)
                .persistWith(new SearchPatientPersister())
                .dontAbortOnStageFailure()
                .withMultipleValidators(1)
                .withMultipleMigrators(1)
                .build();
        try {
            MigrateResult migrateResult = migrator.migrate();
            System.out.println("Migration was " + (migrateResult.hasFailed() ? "successful (with some errors)" : "successful"));
            System.out.println("Stage : " + migrateResult.getStageName() + ". Success count : " + migrateResult.numberOfSuccessfulRecords() +
                    ". Fail count : " + migrateResult.numberOfFailedRecords());
        } catch (MigrationException e) {
            System.out.println("There was an error during migration. " + e.getMessage());
        }

    }

    public void checkFile(String parentDir, String filename) {
        CSVFile<SearchCSVRow> csvfile = null;
        SearchCSVRow rowObject;

        try {
            csvfile = new CSVFile<SearchCSVRow>(parentDir, filename, SearchCSVRow.class);
            csvfile.openForRead();

            int count = 1;
            do{
                rowObject = csvfile.readEntity();
                System.out.println(count + ".");
                System.out.println(rowObject);
                count ++;
                System.out.println("##################################################################################");
            }while(rowObject!=null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }finally{
            if(csvfile!=null) csvfile.close();
        }
    }

    private void validate() {

    }

}
