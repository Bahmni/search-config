package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bahmni.csv.RowResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchPatientPersister implements org.bahmni.csv.EntityPersister<SearchCSVRow> {

    @Override
    public RowResult<SearchCSVRow> persist(SearchCSVRow csvRow) {
//        System.out.println("MMMMMM > "+ csvRow.newCaseNo + " |||| " + csvRow.oldCaseNo + " ||| " + csvRow.firstName + "|||" + csvRow.lastName);

        return new RowResult<SearchCSVRow>(csvRow);
    }

    @Override
    public RowResult<SearchCSVRow> validate(SearchCSVRow csvRow) {

//        System.out.println("VVVVVV > "+ csvRow.newCaseNo + " |||| " + csvRow.oldCaseNo + " ||| " + csvRow.firstName + "|||" + csvRow.lastName);

        if(StringUtils.isEmpty(csvRow.oldCaseNo) &&
                StringUtils.isEmpty(csvRow.newCaseNo))
            return new RowResult<SearchCSVRow>(csvRow, "Old and New Case numbers are Blank.");

        if(StringUtils.isEmpty(csvRow.firstName) &&
                StringUtils.isEmpty(csvRow.middleName) &&
                StringUtils.isEmpty(csvRow.lastName)
                )
            return new RowResult<SearchCSVRow>(csvRow, "All name fields are Blank.");

        Date visitDate;
        try {
            visitDate = DateUtils.parseDateStrictly(csvRow.visit_date, new String[]{"dd/M/yyyy"});
        } catch (ParseException e) {
            return new RowResult<SearchCSVRow>(csvRow, "visit_date is not valid.");
        }

        if(visitDate.after(new Date())){
            return new RowResult<SearchCSVRow>(csvRow, "visit_date must be in the past.");
        }


        return new RowResult<SearchCSVRow>(csvRow);
    }
}
