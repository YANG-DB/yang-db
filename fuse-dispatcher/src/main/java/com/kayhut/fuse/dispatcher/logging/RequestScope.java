package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by roman.margolis on 31/01/2018.
 */
public class RequestScope {
    //region Static
    public static MethodName.MDCWriter of(String requestScope) {
        return new MethodName.MDCWriter(requestScope);
    }

    public static class MDCWriter extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public MDCWriter(String requestScope) {
            super(RequestScope.Converter.key, requestScope);
        }
        //endregion

        //region Properties
        public String getRequestScope() {
            return this.requestScope;
        }
        //endregion

        //region Override Methods
        @Override
        public String toString() {
            return this.requestScope;
        }
        //endregion

        //region Fields
        private String requestScope;
        //endregion
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "requestScope";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().get(key);
        }
        //endregion
    }


}
