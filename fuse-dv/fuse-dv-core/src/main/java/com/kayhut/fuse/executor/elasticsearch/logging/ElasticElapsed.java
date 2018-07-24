package com.kayhut.fuse.executor.elasticsearch.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.slf4j.MDC;

public class ElasticElapsed {
    public static SingleWriter of(long elasticElapsed) {
        return new SingleWriter(elasticElapsed);
    }

    public static TotalWriter add(long elasticElapsed) {
        return new TotalWriter(elasticElapsed);
    }

    public static class SingleWriter implements LogMessage.MDCWriter {
        //region Constructors
        public SingleWriter(long elasticElapsed) {
            this.elasticElapsed = elasticElapsed;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            MDC.put(SingleConverter.key, Long.toString(this.elasticElapsed));
        }
        //endregion

        //region Fields
        private long elasticElapsed;
        //endregion
    }

    public static class TotalWriter implements LogMessage.MDCWriter {
        //region Constructors
        public TotalWriter(long elasticElapsed) {
            this.elasticElapsed = elasticElapsed;
        }
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            String totalString = MDC.get(TotalConverter.key);
            long total = totalString == null ? 0 : Long.parseLong(totalString);

            MDC.put(TotalConverter.key, Long.toString(total + this.elasticElapsed));
        }
        //endregion

        //region Fields
        private long elasticElapsed;
        //endregion
    }

    public static class SingleConverter extends ClassicConverter {
        public static final String key = "elasticElapsed";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }

    public static class TotalConverter extends ClassicConverter {
        public static final String key = "elasticElapsedTotal";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }
}
