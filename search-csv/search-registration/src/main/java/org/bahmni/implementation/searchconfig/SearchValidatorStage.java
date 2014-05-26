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
            System.out.println(csvRow);
            String errorMessage = validate(csvRow);
            if(StringUtils.isNotEmpty(errorMessage)){
                failedValidationList.add(new FailedRowResult<SearchCSVRow>(csvRow, errorMessage));
            }
        }
        csvEntityList.removeAll(failedValidationList);
        return new StageResult(getName(), failedValidationList, csvEntityList);
    }

    private String validate(SearchCSVRow csvRow) {
        StringBuilder errorMessage = new StringBuilder();

        if (StringUtils.isEmpty(csvRow.oldCaseNo) && StringUtils.isEmpty(csvRow.newCaseNo)){
            errorMessage.append("Old and New Case numbers are Blank.");
        }


        if (StringUtils.isEmpty(csvRow.firstName) && StringUtils.isEmpty(csvRow.middleName) &&
               StringUtils.isEmpty(csvRow.lastName)
                ){
            errorMessage.append( "All name fields are Blank.");
        }


        Date visitDate;
        try {
            visitDate = DateUtils.parseDateStrictly(csvRow.visit_date, new String[]{"dd/M/yyyy"});

            if (visitDate.after(new Date())) {
                errorMessage.append("visit_date must be in the past.");
            }
        } catch (ParseException e) {
            errorMessage.append("visit_date is not valid.");
        }


        return errorMessage.toString();
    }
}
