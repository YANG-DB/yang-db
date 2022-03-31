package com.yangdb.logging.log4j2.converters;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
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

import com.yangdb.logging.mdc.Epoch;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

import java.util.concurrent.TimeUnit;

public class ElapsedFrom {
    @Plugin(name = "elapsedFrom", category = "Converter")
    @ConverterKeys({"elapsedFrom"})
    public static class Converter extends LogEventPatternConverter {
        public static Converter newInstance(final String[] options) {
            TimeUnit timeUnit = TimeUnit.MILLISECONDS;
            if (options == null || options.length == 0) {
                try {
                    TimeUnit.valueOf(options[0]);
                } catch (IllegalArgumentException ex) {
                    timeUnit = TimeUnit.NANOSECONDS;
                }
            }

            return new Converter("elapsedFrom", "elapsedFrom", timeUnit);
        }

        protected Converter(String name, String style, TimeUnit timeUnit) {
            super(name, style);
            this.timeUnit = timeUnit;
        }

        //region Override Methods
        @Override
        public void format(LogEvent logEvent, StringBuilder sb) {
            String start = logEvent.getContextData().getValue(Epoch.FromNanos.key);
            if (start == null) {
                sb.append("0");
            } else {
                long elapsed = logEvent.getNanoTime() - Long.parseLong(start);
                elapsed = this.timeUnit.convert(elapsed, TimeUnit.NANOSECONDS);
                sb.append(elapsed);
            }
        }
        //endregion

        //region Fields
        private TimeUnit timeUnit;
        //endregion
    }
}
