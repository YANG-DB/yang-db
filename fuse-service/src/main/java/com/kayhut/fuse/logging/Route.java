package com.kayhut.fuse.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.kayhut.fuse.dispatcher.logging.LogMessage;

public class Route {
    public static LogMessage.MDCWriter of(String route) {
        return new LogMessage.MDCWriter.KeyValue(RequestId.Converter.key, route);
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "route";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }
}
