package com.yangdb.fuse.dispatcher.logging;

/*-
 *
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

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
