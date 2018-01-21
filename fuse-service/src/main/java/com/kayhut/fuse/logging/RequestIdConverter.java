package com.kayhut.fuse.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.function.Supplier;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class RequestIdConverter extends ClassicConverter {
    public static final String key = "requestId";

    //region ClassicConverter Implementation
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return iLoggingEvent.getMDCPropertyMap().get(key);
    }
    //endregion
}
