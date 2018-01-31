package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by roman.margolis on 30/01/2018.
 */
public class MethodName{
    //region Static
    public static MDCWriter of(String methodName) {
        return new MDCWriter(methodName);
    }
    //endregion

    public static class MDCWriter extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public MDCWriter(String methodName) {
            super(Converter.key, methodName);
        }
        //endregion

        //region Properties
        public String getMethodName() {
            return this.methodName;
        }
        //endregion

        //region Override Methods
        @Override
        public String toString() {
            return this.methodName;
        }
        //endregion

        //region Fields
        private String methodName;
        //endregion
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "methodName";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().get(key);
        }
        //endregion
    }
}
