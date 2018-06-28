package com.kayhut.fuse.executor.elasticsearch.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.slf4j.MDC;

public class ElasticElapsedTotal {
    public static LogMessage.MDCWriter start() {
        return new StartWriter(System.currentTimeMillis());
    }

    public static LogMessage.MDCWriter startDeffered() {
        return new StartWriter(-1);
    }

    public static LogMessage.MDCWriter stop() {
        return new ElapsedWriter();
    }

    public static class StartWriter implements LogMessage.MDCWriter {
        //region Static
        public static final String key = "elasticOperationStart";
        //endregion

        //region Constructors
        public StartWriter(long fromEpoch) {
            this.fromEpoch = fromEpoch;
        }
        //endregion

        //region LogMessage.MDCWriter Implementation
        @Override
        public void write() {
            MDC.put(key, Long.toString(this.fromEpoch == -1 ? System.currentTimeMillis() : this.fromEpoch));
        }
        //endregion

        //region Fields
        private long fromEpoch;
        //endregion
    }

    public static class ElapsedWriter implements LogMessage.MDCWriter {
        //region Static
        public static final String key = "elasticElapsedTotal";
        //endregion

        //region MDCWriter Implementation
        @Override
        public void write() {
            String startFromEpochString = MDC.get(StartWriter.key);
            if (startFromEpochString == null) {
                return;
            }

            long elapsed = System.currentTimeMillis() - Long.parseLong(startFromEpochString);

            String elasticElapsedTotalString = MDC.get(key);
            long elasticElapsedTotalCurrent = elasticElapsedTotalString == null ? 0 : Long.parseLong(elasticElapsedTotalString);


            MDC.put(key, Long.toString(elasticElapsedTotalCurrent + elapsed));
        }
        //endregion
    }

    public static class Converter extends ClassicConverter {
        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(ElapsedWriter.key, "0");
        }
        //endregion
    }
}
