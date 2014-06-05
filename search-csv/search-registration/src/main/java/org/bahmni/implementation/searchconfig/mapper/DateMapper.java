package org.bahmni.implementation.searchconfig.mapper;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.implementation.searchconfig.SearchCSVRow;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateMapper {
    public static Date getDateFromOldCaseNumber(SearchCSVRow csvRow) {
        int january = 0;
        String oldCaseNumber = csvRow.oldCaseNo;
        String[] caseNumberParts = oldCaseNumber.split("/");
        String yearOfRegistrationString;
        if(caseNumberParts[1].length() == 2){
            yearOfRegistrationString = "20" + caseNumberParts[1];
        } else {
            yearOfRegistrationString = "200" + caseNumberParts[1];
        }
        Integer yearOfRegistration = Integer.parseInt(yearOfRegistrationString);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, yearOfRegistration);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, january);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getDateFromVisitDate(SearchCSVRow csvRow) {
        try{
            Date date = DateUtils.parseDateStrictly(csvRow.visit_date, new String[]{"dd/M/yy"});
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 10);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (ParseException e) {
            return null;
        }

    }
}
