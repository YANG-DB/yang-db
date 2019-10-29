package com.yangdb.fuse.dispatcher.logging;

/*-
 * #%L
 * fuse-core
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
