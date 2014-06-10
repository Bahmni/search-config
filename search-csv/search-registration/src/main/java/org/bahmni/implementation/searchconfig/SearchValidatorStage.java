package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.SimpleStage;
import org.bahmni.csv.StageResult;
import org.bahmni.csv.exception.MigrationException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchValidatorStage implements SimpleStage<SearchCSVRow> {
    private static Logger logger = org.apache.log4j.Logger.getLogger(SearchValidatorStage.class);

    @Override
    public String getName() {
        return "SEARCH_VALIDATOR";
    }

    @Override
    public boolean canRunInParallel() {
        return true;
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
        return new StageResult(getName(), failedValidationList, csvEntityList);
    }

    private String validate(SearchCSVRow csvRow) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        validateCaseNumbers(csvRow, errorMessageBuilder);
        validateName(csvRow, errorMessageBuilder);
        validateVisitDate(csvRow, errorMessageBuilder);
        validateAge(csvRow, errorMessageBuilder);
        validateAndSanitizeRegistrationFee(csvRow, errorMessageBuilder);
        return errorMessageBuilder.toString();
    }

    private void validateAndSanitizeRegistrationFee(SearchCSVRow csvRow, StringBuilder errorMessageBuilder) {
        if(StringUtils.isEmpty(csvRow.fees))
            return;
        if (IsNotDigitsOrFree(csvRow)) {
            errorMessageBuilder.append("Fee should be a number or 'free'.");
        }
        sanitizeRegistrationFee(csvRow);
    }

    private void sanitizeRegistrationFee(SearchCSVRow csvRow) {
        if(isFree(csvRow)){
            csvRow.fees = "0";
        }
    }

    private boolean IsNotDigitsOrFree(SearchCSVRow csvRow) {
        boolean isDigits = csvRow.fees.matches("\\d+");
        if(!isDigits)
            return false;
        return isFree(csvRow);
    }

    private boolean isFree(SearchCSVRow csvRow) {
        return csvRow.fees.trim().replaceAll(" ", "").equalsIgnoreCase("free");
    }

    private void validateAge(SearchCSVRow csvRow, StringBuilder errorMessageBuilder) {
        if (StringUtils.isEmpty(csvRow.age))
            return;
        csvRow.age = csvRow.age.trim().replaceAll(" ", "");
        if (!csvRow.age.matches("(?:\\d+[ymd]\\s*)+")) {
            try {
                int age = Integer.parseInt(csvRow.age);
                if (age > 100 || age < 0) {
                    errorMessageBuilder.append("Age cannot be larger than 100 or lesser than 0.");
                }
            } catch (NumberFormatException ex) {
                errorMessageBuilder.append("Age is not in required format.");
            }
            return;
        }

        String[] split = csvRow.age.split("(?<=[ymd])\\s*");
        Integer years = 0, months = 0, days = 0;
        for (String s : split) {
            if (s.endsWith("y")) {
                years = Integer.parseInt(s.replace('y', ' ').trim());
            } else if (s.endsWith("m")) {
                months = Integer.parseInt(s.replace('m', ' ').trim());
            }
            if (s.endsWith("d")) {
                days = Integer.parseInt(s.replace('d', ' ').trim());
            }
        }
        days = days + months * 30 + years * 365;
        if (days > 36500) {
            errorMessageBuilder.append("Age cannot be larger than 100.");
        }
    }

    private void validateCaseNumbers(SearchCSVRow csvRow, StringBuilder errorMessageBuilder) {
        if (StringUtils.isEmpty(csvRow.oldCaseNo) && StringUtils.isEmpty(csvRow.newCaseNo)) {
            errorMessageBuilder.append("Old and New Case numbers are Blank.");
        } else if (StringUtils.isNotEmpty(csvRow.oldCaseNo) && StringUtils.isNotEmpty(csvRow.newCaseNo)) {
            csvRow.oldCaseNo = csvRow.oldCaseNo.trim().replaceAll(" ", "");
            csvRow.newCaseNo = csvRow.newCaseNo.trim().replaceAll(" ", "");
            if (StringUtils.isNotEmpty(csvRow.oldCaseNo) && StringUtils.isNotEmpty(csvRow.newCaseNo)) {
                errorMessageBuilder.append("Both old and new Case numbers are entered.");
            }
        } else {
            String caseNumberFormat = "\\d+\\/\\d{1,2}";
            if (StringUtils.isNotEmpty(csvRow.newCaseNo)) {
                csvRow.newCaseNo = csvRow.newCaseNo.trim();
                if (!csvRow.newCaseNo.matches(caseNumberFormat)) {
                    errorMessageBuilder.append("New Case number is not in the correct format.");
                }
            }
            if (StringUtils.isNotEmpty(csvRow.oldCaseNo)) {
                csvRow.oldCaseNo = csvRow.oldCaseNo.trim();
                if (!csvRow.oldCaseNo.matches(caseNumberFormat)) {
                    errorMessageBuilder.append("Old Case number is not in the correct format.");
                }
            }
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
            logger.info("visit_date is in invalid format: " + csvRow.visit_date);
        }
    }

    private void validateName(SearchCSVRow csvRow, StringBuilder errorMessage) {
        if (StringUtils.isEmpty(csvRow.firstName)) {
            errorMessage.append("FirstName is mandatory.");
        }
        if (StringUtils.isEmpty(csvRow.lastName) && StringUtils.isEmpty(csvRow.middleName)) {
            errorMessage.append("Either Middle name or last name should be present.");
        }
    }
}
