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

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class MDCConverter extends LogEventPatternConverter {
    public static LogEventPatternConverter newInstance(final String[] options) {
        String key = options == null || options.length == 0 ?
                "key" :
                options[0];

        String defaultValue = options == null || options.length < 2 ?
                null :
                options[1];

        return new MDCConverter("mdc", "mdc", key, defaultValue);
    }

    protected MDCConverter(String name, String style, String key, String defaultValue) {
        super(name, style);
        this.key = key;
        this.defaultValue = defaultValue;
    }

    //region LogEventPatternConverter Implementation
    @Override
    public void format(LogEvent logEvent, StringBuilder sb) {
        String value = logEvent.getContextData().getValue(this.key);
        value = value == null ? this.defaultValue : value;
        if (value != null) {
            sb.append(value);
        }
    }
    //endregion

    //region Fields
    protected String key;
    protected String defaultValue;
    //endregion
}
