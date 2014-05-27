package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bahmni.csv.*;
import org.bahmni.csv.exception.MigrationException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchValidatorStage implements SimpleStage<SearchCSVRow> {
    @Override
    public String getName() {
        return "SEARCH_VALIDATOR";
    }

    @Override
    public boolean canRunInParallel() {
        return false;
    }

    @Override
    public StageResult execute(List<SearchCSVRow> csvEntityList) throws MigrationException {
        List<FailedRowResult<SearchCSVRow>> failedValidationList = new ArrayList<FailedRowResult<SearchCSVRow>>();

        for (SearchCSVRow csvRow : csvEntityList) {
            String errorMessage = validate(csvRow);
            if (StringUtils.isNotEmpty(errorMessage)) {
                failedValidationList.add(new FailedRowResult<SearchCSVRow>(csvRow, getName() + ":" + errorMessage));
            }
        }
        csvEntityList.removeAll(failedValidationList);
        return new StageResult(getName(), failedValidationList, csvEntityList);
    }

    private String validate(SearchCSVRow csvRow) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        validateCaseNumbers(csvRow, errorMessageBuilder);
        validateName(csvRow, errorMessageBuilder);
        validateVisitDate(csvRow, errorMessageBuilder);
        return errorMessageBuilder.toString();
    }

    private void validateCaseNumbers(SearchCSVRow csvRow, StringBuilder errorMessageBuilder) {
        if (StringUtils.isEmpty(csvRow.oldCaseNo) && StringUtils.isEmpty(csvRow.newCaseNo)) {
            errorMessageBuilder.append("Old and New Case numbers are Blank.");
        }
    }

    private void validateVisitDate(SearchCSVRow csvRow, StringBuilder errorMessage) {
        Date visitDate;
        try {
            visitDate = DateUtils.parseDateStrictly(csvRow.visit_date, new String[]{"dd/M/yyyy"});

            if (visitDate.after(new Date())) {
                errorMessage.append("visit_date must be in the past.");
            }
        } catch (ParseException e) {
            errorMessage.append("visit_date is not valid.");
        }
    }

    private void validateName(SearchCSVRow csvRow, StringBuilder errorMessage) {
        if (StringUtils.isEmpty(csvRow.firstName)) {
            errorMessage.append("FirstName is mandatory");
        }
        if (StringUtils.isEmpty(csvRow.lastName)) {
            errorMessage.append("LastName is mandatory");
        }
    }
}
