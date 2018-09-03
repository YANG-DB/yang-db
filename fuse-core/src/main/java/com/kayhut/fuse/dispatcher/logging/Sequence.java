package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

public class Sequence {
    //region Static
    public static LogMessage.MDCWriter of(int value) {
        return new InitialValue(value);
    }

    public static LogMessage.MDCWriter incr() {
        return new Incrementor();
    }
    //endregion

    public static class InitialValue extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public InitialValue(int value) {
            super(Converter.key, Integer.toString(value));
        }
        //endregion
    }

    public static class Incrementor extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public Incrementor() {
            super(Converter.key, incrOrDefault(1));
        }
        //endregion

        private static String incrOrDefault(int value) {
            String sequenceString = MDC.get(Converter.key);
            if (sequenceString == null) {
                return Integer.toString(value);
            }

            return Integer.toString(Integer.parseInt(sequenceString) + 1);
        }
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "sequence";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().get(key);
        }
        //endregion
    }
}
