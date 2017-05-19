package com.kayhut.fuse.generator.util;

import java.util.Date;

/**
 * Created by benishue on 16-May-17.
 */
public class DateUtil {

    private DateUtil() {
        throw new IllegalAccessError("Utility class");
    }


    public static Date addMinutesToDate(Date beforeTime, int minutes){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
        long curTimeInMs = beforeTime.getTime();
        return new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
    }

}
