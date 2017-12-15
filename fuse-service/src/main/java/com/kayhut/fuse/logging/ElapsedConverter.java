package com.kayhut.fuse.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

/**
 * Created by Roman on 12/15/2017.
 */
public class ElapsedConverter extends ClassicConverter {
    public static final String key = "startEpoch";

    //region ClassicConverter Implementation
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        String start = MDC.get(key);
        if (start == null) {
            return "";
        }

        long elapsed = System.currentTimeMillis() - Long.parseLong(start);
        return Long.toString(elapsed);
    }
    //endregion
}
