package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

/**
 * Created by roman.margolis on 30/01/2018.
 */
public class ElapsedFrom {
    public static LogMessage.MDCWriter of(long fromEpoch) {
        return new MDCWriter(fromEpoch);
    }

    public static LogMessage.MDCWriter now() {
        return new MDCWriter(System.currentTimeMillis());
    }

    public static LogMessage.MDCWriter deferredNow() {
        return new MDCWriter(-1);
    }

    public static class MDCWriter implements LogMessage.MDCWriter {
        //region Static
        public static final String key = "fromEpochNext";
        //endegion

        //region Constructors
        public MDCWriter(long fromEpoch) {
            this.fromEpoch = fromEpoch;
        }
        //endregion

        //region LogMessage.MDCWriter Implementation
        @Override
        public void write() {
            String fromEpochNextString = MDC.get(key);
            if (fromEpochNextString != null) {
                MDC.put(Converter.key, fromEpochNextString);
            }

            MDC.put(key, Long.toString(this.fromEpoch == -1 ? System.currentTimeMillis() : this.fromEpoch));
        }
        //endregion

        //region Fields
        private long fromEpoch;
        //endregion
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "fromEpoch";

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
