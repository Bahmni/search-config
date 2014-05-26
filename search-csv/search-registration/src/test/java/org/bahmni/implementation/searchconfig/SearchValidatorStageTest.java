package org.bahmni.implementation.searchconfig;

import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.RowResult;
import org.bahmni.csv.StageResult;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Gurpreet on 26/05/14.
 */
public class SearchValidatorStageTest {
    @Test
    public void validate_shouldFailFor_InvalidDateFormatForVisitDate() throws Exception {
        SearchValidatorStage validatorStage = new SearchValidatorStage();
        SearchCSVRow row = searchCsvBuilder();
        StageResult<SearchCSVRow> validationResult;

        row.visit_date = "33/01/2012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals("visit_date is not valid.", failedRowResult.getErrorMessage());

        row.visit_date = "1-01/2012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals("visit_date is not valid.", failedRowResult.getErrorMessage());

        row.visit_date = "31-01-2012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals("visit_date is not valid.", failedRowResult.getErrorMessage());
    }

    @Test
    public void validate_shouldFailFor_FutureVisitDate(){

        SearchValidatorStage validatorStage = new SearchValidatorStage();
        SearchCSVRow row = searchCsvBuilder();
        StageResult<SearchCSVRow> validationResult;

        row.visit_date = "31/01/20012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals("visit_date must be in the past.", failedRowResult.getErrorMessage());

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
