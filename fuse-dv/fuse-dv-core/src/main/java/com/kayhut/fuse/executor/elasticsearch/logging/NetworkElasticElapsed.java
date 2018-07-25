package com.kayhut.fuse.executor.elasticsearch.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.slf4j.MDC;

public class NetworkElasticElapsed {
    public static StartWriter start() {
        return new StartWriter(System.currentTimeMillis());
    }

    public static SingleWriter stop() {
        return new SingleWriter();
    }

    public static TotalWriter stopTotal() {
        return new TotalWriter();
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
            MDC.put(key, Long.toString(this.fromEpoch));
        }
        //endregion

        //region Fields
        private long fromEpoch;
        //endregion
    }

    public static class SingleWriter implements LogMessage.MDCWriter {
        //region MDCWriter Implementation
        @Override
        public void write() {
            String startFromEpochString = MDC.get(StartWriter.key);
            if (startFromEpochString == null) {
                return;
            }

            long elapsed = System.currentTimeMillis() - Long.parseLong(startFromEpochString);
            MDC.put(SingleConverter.key, Long.toString(elapsed));
        }
        //endregion
    }


    public static class TotalWriter implements LogMessage.MDCWriter {
        //region MDCWriter Implementation
        @Override
        public void write() {
            String startFromEpochString = MDC.get(StartWriter.key);
            if (startFromEpochString == null) {
                return;
            }

            long elapsed = System.currentTimeMillis() - Long.parseLong(startFromEpochString);

            String elasticElapsedTotalString = MDC.get(TotalConverter.key);
            long elasticElapsedTotalCurrent = elasticElapsedTotalString == null ? 0 : Long.parseLong(elasticElapsedTotalString);


            MDC.put(TotalConverter.key, Long.toString(elasticElapsedTotalCurrent + elapsed));
        }
        //endregion
    }

    public static class SingleConverter extends ClassicConverter {
        public static final String key = "networkElasticElapsed";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }

    public static class TotalConverter extends ClassicConverter {
        public static final String key = "networkElasticElapsedTotal";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }
}
