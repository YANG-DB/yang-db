package com.yangdb.logging.log4j2.slf4j;

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

import com.yangdb.logging.log4j2.ExtendedThreadContext;
import com.yangdb.logging.slf4j.api.ExtendedMDCAdapter;

import java.util.Map;

public class Log4jExtendedMDCAdapter implements ExtendedMDCAdapter {
    //region ExtendedMDCAdapter Implementation
    @Override
    public void put(final String key, final Object val) {
        ExtendedThreadContext.put(key, val);
    }

    @Override
    public <V> V get(final String key) {
        return ExtendedThreadContext.get(key);
    }

    @Override
    public void remove(final String key) {
        ExtendedThreadContext.remove(key);
    }

    @Override
    public void clear() {
        ExtendedThreadContext.clearMap();
    }

    @Override
    public Map<String, Object> getCopyOfContextMap() {
        return ExtendedThreadContext.getContext();
    }

    @Override
    @SuppressWarnings("unchecked") // nothing we can do about this, restricted by SLF4J API
    public void setContextMap(@SuppressWarnings("rawtypes") final Map map) {
        ExtendedThreadContext.clearMap();
        for (final Map.Entry<String, Object> entry : ((Map<String, Object>) map).entrySet()) {
            ExtendedThreadContext.put(entry.getKey(), entry.getValue());
        }
    }
    //endregion
}
