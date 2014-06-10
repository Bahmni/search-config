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

    @Test
    public void validate_shouldFailWhen_BothOldAndNewCaseNumbersArePresent() {
        row.oldCaseNo="1234/11";
        row.newCaseNo="1234/11";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Both old and new Case numbers are entered.", failedRowResult.getErrorMessage());
    }

    @Test
    public void validate_shouldFailWhen_CaseNumberIsNotInTheFormatRequired() {
        row.oldCaseNo="12/12222";
        row.newCaseNo="";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Old Case number is not in the correct format.", failedRowResult.getErrorMessage());

        row.newCaseNo="12/12222";
        row.oldCaseNo="";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "New Case number is not in the correct format.", failedRowResult.getErrorMessage());
    }

    @Test
    public void validate_shouldPass_WhenCaseNumberIsInTheRightFormat() {
        row.newCaseNo="1234/12";
        row.oldCaseNo="";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.newCaseNo="1234/2";
        row.oldCaseNo="";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.oldCaseNo="4321/08";
        row.newCaseNo="";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.oldCaseNo="5432/2";
        row.newCaseNo="";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

    }

    @Test
    public void validate_shouldFailWhen_AgeIsNotInRequiredFormat() {
        row.age = "12yrs";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Age is not in required format.", failedRowResult.getErrorMessage());

        row.age = "12y15months400days";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Age is not in required format.", failedRowResult.getErrorMessage());

        row.age = "2.5";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Age is not in required format.", failedRowResult.getErrorMessage());
    }

    @Test
    public void validate_shouldPassWhen_AgeIsSpecifiedInTheRightFormat() {
        row.age = "1y 5m 4d";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "80y 50m 46d";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "1y5m4d";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "              1y5m4d";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "1y         5m4d";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "100y";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "100";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "25";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());

        row.age = "6 m";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());
    }

    @Test
    public void validate_shouldFailWhen_AgeIsGreaterThan100() {
        row.age = "100y 1d";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        FailedRowResult<SearchCSVRow> failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Age cannot be larger than 100.", failedRowResult.getErrorMessage());

        row.age = "5200m";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Age cannot be larger than 100.", failedRowResult.getErrorMessage());

        row.age = "5200";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(1, validationResult.getFailureCount());
        failedRowResult = validationResult.getFailedCSVEntities().get(0);
        assertEquals(stageName + "Age cannot be larger than 100 or lesser than 0.", failedRowResult.getErrorMessage());
    }

    @Test
    public void shouldValidateRegistrationFee() {
        row.fees = "10";
        validationResult = validatorStage.execute(Arrays.asList(row));
        assertEquals(0, validationResult.getFailureCount());
    }
}
