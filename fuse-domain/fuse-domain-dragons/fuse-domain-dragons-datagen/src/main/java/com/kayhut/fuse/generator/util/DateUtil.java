package com.kayhut.fuse.generator.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by benishue on 16-May-17.
 */
public class DateUtil {

    private DateUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static Date addMinutesToDate(Date date, int minutes){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
        long curTimeInMs = date.getTime();
        return new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
    }

    public static Date addYearsToDate(Date date, int years){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, years);
        return c.getTime();
    }

}
