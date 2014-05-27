package org.bahmni.implementation.searchconfig;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class SearchMigratorTest {

    @Test
    public void testMigrator(){
        String csvFileName = "sampleReg.csv";

        SearchMigrator migrator = new SearchMigrator();
        migrator.process(getCSVFolderPath(csvFileName), csvFileName);
    }

    private String getCSVFolderPath(String testResourceName){
        URL resource = this.getClass().getClassLoader().getResource(testResourceName);
        File file = null;
        try {
            file = new File(resource.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return file.getParent();
    }
}
