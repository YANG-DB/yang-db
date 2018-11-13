package com.kayhut.fuse.dispatcher.logging;

/*-
 * #%L
 * fuse-core
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
import org.slf4j.MDC;

public class Sequence {
    //region Static
    public static LogMessage.MDCWriter of(int value) {
        return new InitialValue(value);
    }

    public static LogMessage.MDCWriter incr() {
        return new Incrementor();
    }
    //endregion

    public static class InitialValue extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public InitialValue(int value) {
            super(Converter.key, Integer.toString(value));
        }
        //endregion
    }

    public static class Incrementor extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public Incrementor() {
            super(Converter.key, incrOrDefault(1));
        }
        //endregion

        private static String incrOrDefault(int value) {
            String sequenceString = MDC.get(Converter.key);
            if (sequenceString == null) {
                return Integer.toString(value);
            }

            return Integer.toString(Integer.parseInt(sequenceString) + 1);
        }
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "sequence";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().get(key);
        }
        //endregion
    }
}
