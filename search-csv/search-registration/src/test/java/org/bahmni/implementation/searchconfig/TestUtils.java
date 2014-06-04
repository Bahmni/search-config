package org.bahmni.implementation.searchconfig;

public class TestUtils {
    public static SearchCSVRow searchCsvBuilder() {
        SearchCSVRow searchCSVRow = new SearchCSVRow();
        searchCSVRow.firstName = "first";
        searchCSVRow.middleName = "middle";
        searchCSVRow.lastName = "last";
        searchCSVRow.newCaseNo = "123/12";
        searchCSVRow.visit_date = "11/01/2012";
        searchCSVRow.gender = "F";
        return searchCSVRow;
    }
}
