package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.StringJoiner;

/**
 * Created by roman.margolis on 31/01/2018.
 */
public class RequestIdByScope {
    //region Static
    public static RequestIdByScope.MDCWriter of(String requestScope) {
        return new RequestIdByScope.MDCWriter(requestScope);
    }

    public static class MDCWriter extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public MDCWriter(String requestScope) {
            super(RequestIdByScope.Converter.key, requestScope);
        }
        //endregion
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "requestScope";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }

    public static class Builder {
        private StringJoiner builder;
        private Builder(String query) {
            builder = new StringJoiner(".");
            builder.add(query);
        }

        public Builder cursor(String c) {
            builder.add(c);
            return this;
        }

        public Builder page(String p) {
            builder.add(p);
            return this;
        }


        public String get() {
            return builder.toString();
        }

        public static Builder query(String q) {
            return new Builder(q);
        }
    }

}
