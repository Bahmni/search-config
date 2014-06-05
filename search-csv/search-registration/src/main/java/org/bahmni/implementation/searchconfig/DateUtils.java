package org.bahmni.implementation.searchconfig;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static String stringify(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static String truncateTimeComponent(String date) {
        String[] dateParts = date.split(" ");
        return dateParts[0];
    }

    public static String getDiff(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuilder builder = new StringBuilder();
        builder.append(diffDays + " days, ");
        builder.append(diffHours + " hours, ");
        builder.append(diffMinutes + " minutes, ");
        builder.append(diffSeconds + " seconds.");

        return builder.toString();
    }
}
