package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.time.DateUtils;
import org.bahmni.csv.RowResult;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchPatientPersisterTest {
    @Test
    public void validate_shouldFailFor_InvalidDateFormatForVisitDate() throws Exception {
        SearchPatientPersister searchPatientPersister = new SearchPatientPersister();
        SearchCSVRow row = searchCsvBuilder();
        RowResult<SearchCSVRow> validationResult;

        row.visit_date = "33/01/2012";
        validationResult = searchPatientPersister.validate(row);
        assertFalse(validationResult.isSuccessful());
        assertEquals("visit_date is not valid.",validationResult.getErrorMessage());

        row.visit_date = "1-01/2012";
        validationResult = searchPatientPersister.validate(row);
        assertFalse(validationResult.isSuccessful());
        assertEquals("visit_date is not valid.",validationResult.getErrorMessage());

        row.visit_date = "31-01-2012";
        validationResult = searchPatientPersister.validate(row);
        assertFalse(validationResult.isSuccessful());
        assertEquals("visit_date is not valid.",validationResult.getErrorMessage());
    }

    @Test
    public void validate_shouldFailFor_FutureVisitDate(){
        SearchPatientPersister searchPatientPersister = new SearchPatientPersister();
        SearchCSVRow row = searchCsvBuilder();
        RowResult<SearchCSVRow> validationResult;

        row.visit_date = "30/01/20012";
        validationResult = searchPatientPersister.validate(row);
        assertFalse(validationResult.isSuccessful());
        assertEquals("visit_date must be in the past.",validationResult.getErrorMessage());

    }

    private SearchCSVRow searchCsvBuilder() {
        SearchCSVRow searchCSVRow = new SearchCSVRow();
        searchCSVRow.firstName = "first";
        searchCSVRow.middleName = "middle";
        searchCSVRow.lastName = "last";
        searchCSVRow.newCaseNo = "123/12";
        searchCSVRow.visit_date = "11/01/2012";
        return searchCSVRow;
    }
}
