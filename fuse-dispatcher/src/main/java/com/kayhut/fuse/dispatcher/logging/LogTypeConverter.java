package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by roman.margolis on 09/01/2018.
 */
public class LogTypeConverter extends ClassicConverter {
    public static final String key = "logType";

    //region ClassicConverter Implementation
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return iLoggingEvent.getMDCPropertyMap().get(key);
    }
    //endregion
}