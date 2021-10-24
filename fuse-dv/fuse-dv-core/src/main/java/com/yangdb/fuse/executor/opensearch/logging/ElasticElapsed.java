package com.yangdb.fuse.executor.opensearch.logging;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.yangdb.fuse.dispatcher.logging.LogMessage;
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
