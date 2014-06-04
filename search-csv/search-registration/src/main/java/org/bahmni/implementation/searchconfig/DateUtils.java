package org.bahmni.implementation.searchconfig;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static String stringify(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static String truncateTimeComponent(String date) {
        String[] dateParts = date.split(" ");
        return dateParts[0];
    }
}
