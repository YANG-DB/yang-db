package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by roman.margolis on 30/01/2018.
 */
public class Elapsed {
    public static LogMessage.MDCWriter of(long startEpoch) {
        return new LogMessage.MDCWriter.KeyValue(Converter.key, Long.toString(startEpoch));
    }

    public static LogMessage.MDCWriter now() {
        return new LogMessage.MDCWriter.KeyValue(Converter.key, Long.toString(System.currentTimeMillis()));
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "startEpoch";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            String start = iLoggingEvent.getMDCPropertyMap().get(key);
            if (start == null) {
                return "0";
            }

            long elapsed = iLoggingEvent.getTimeStamp() - Long.parseLong(start);
            return Long.toString(elapsed);
        }
        //endregion
    }
}
