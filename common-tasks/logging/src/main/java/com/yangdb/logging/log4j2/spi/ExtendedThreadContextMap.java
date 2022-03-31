package com.yangdb.logging.log4j2.spi;

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

import java.util.Map;

public interface ExtendedThreadContextMap extends ReadOnlyExtendedThreadContextMap {
    /**
     * Puts a context value (the <code>o</code> parameter) as identified
     * with the <code>key</code> parameter into the current thread's
     * context map.
     *
     * <p>If the current thread does not have a context map it is
     * created as a side effect.</p>
     * @param key The key name.
     * @param value The key value.
     */
    void put(final String key, final Object value);

    /**
     * Puts all given context map entries into the current thread's
     * context map.
     *
     * <p>If the current thread does not have a context map it is
     * created as a side effect.</p>
     * @param map The map.
     * @since 2.7
     */
    void putAll(final Map<String, Object> map);

    /**
     * Removes the the context identified by the <code>key</code>
     * parameter.
     * @param key The key to remove.
     */
    void remove(final String key);

    /**
     * Removes all given context map keys from the current thread's context map.
     *
     * <p>If the current thread does not have a context map it is
     * created as a side effect.</p>

     * @param keys The keys.
     * @since 2.8
     */
    void removeAll(final Iterable<String> keys);

}
