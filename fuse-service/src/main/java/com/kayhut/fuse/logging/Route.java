package com.kayhut.fuse.logging;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.kayhut.fuse.dispatcher.logging.LogMessage;

public class Route {
    public static LogMessage.MDCWriter of(String route) {
        return new LogMessage.MDCWriter.KeyValue(Converter.key, route);
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "route";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }
}
