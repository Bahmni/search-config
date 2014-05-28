package org.bahmni.implementation.searchconfig;

import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.StageResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SearchValidatorStageTest {
    String stageName;
    private SearchValidatorStage validatorStage;
    private SearchCSVRow row;
    private StageResult<SearchCSVRow> validationResult;

    @Before
    public void setUp() throws Exception {
        validatorStage = new SearchValidatorStage();
        row = TestUtils.searchCsvBuilder();
        stageName = validatorStage.getName() + ":";
    }

    @Test
    public void validate_shouldFailFor_InvalidDateFormatForVisitDate() throws Exception {
        row.visit_date = "33/01/2012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "visit_date is not valid.", failedRowResult.getErrorMessage());

        row.visit_date = "1-01/2012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "visit_date is not valid.", failedRowResult.getErrorMessage());

        row.visit_date = "31-01-2012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName +"visit_date is not valid.", failedRowResult.getErrorMessage());
    }

    @Test
    public void validate_shouldFailFor_FutureVisitDate(){
        row.visit_date = "31/01/20012";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName +"visit_date must be in the past.", failedRowResult.getErrorMessage());

    }

    @Test
    public void validate_shouldFailFor_EmptyGivenName() {
        row.firstName = "";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "FirstName is mandatory.", failedRowResult.getErrorMessage());
    }

    @Test
    public void validate_shouldFailFor_EmptyLastAndMiddle() {
        row.middleName = "";
        row.lastName = "";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Either Middle name or last name should be present.", failedRowResult.getErrorMessage());
    }



}
