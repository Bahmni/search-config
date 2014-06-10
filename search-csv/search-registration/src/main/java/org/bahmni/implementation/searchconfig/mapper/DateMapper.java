package org.bahmni.implementation.searchconfig.mapper;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.implementation.searchconfig.SearchCSVRow;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateMapper {
    public static Date getDateFromOldCaseNumber(SearchCSVRow csvRow) {
        return getVisitDateFromCaseNumber(csvRow.oldCaseNo);
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

    public static String getVisitStopDatetimeFor(Date visitStartDateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(visitStartDateTime);
        cal.add(Calendar.HOUR, 4);
        return org.bahmni.implementation.searchconfig.DateUtils.stringify(cal.getTime());
    }

    public static Date getVisitDate(SearchCSVRow csvRow, boolean fromOldCaseNumber) {
        if(fromOldCaseNumber){
            return DateMapper.getDateFromOldCaseNumber(csvRow);
        }
        return DateMapper.getDateFromVisitDate(csvRow);
    }

    public static Date getVisitDateFromCaseNumber(String caseNo) {
        int january = 0;
        String[] caseNumberParts = caseNo.split("/");
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
}
