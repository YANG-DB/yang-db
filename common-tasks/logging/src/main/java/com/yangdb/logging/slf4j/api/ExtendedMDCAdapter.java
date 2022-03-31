package com.yangdb.logging.slf4j.api;

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

public interface ExtendedMDCAdapter {
    /**
     * Put a context value (the <code>val</code> parameter) as identified with
     * the <code>key</code> parameter into the current thread's context map.
     * The <code>key</code> parameter cannot be null. The <code>val</code> parameter
     * can be null only if the underlying implementation supports it.
     *
     * <p>If the current thread does not have a context map it is created as a side
     * effect of this call.
     */
    void put(String key, Object val);

    /**
     * Get the context identified by the <code>key</code> parameter.
     * The <code>key</code> parameter cannot be null.
     *
     * @return the object value identified by the <code>key</code> parameter.
     */
    <T> T get(String key);

    /**
     * Remove the the context identified by the <code>key</code> parameter.
     * The <code>key</code> parameter cannot be null.
     *
     * <p>
     * This method does nothing if there is no previous value
     * associated with <code>key</code>.
     */
    void remove(String key);

    /**
     * Clear all entries in the MDC.
     */
    void clear();

    /**
     * Return a copy of the current thread's context map, with keys and
     * values of type String. Returned value may be null.
     *
     * @return A copy of the current thread's context map. May be null.
     * @since 1.5.1
     */
    Map<String, Object> getCopyOfContextMap();

    /**
     * Set the current thread's context map by first clearing any existing
     * map and then copying the map passed as parameter. The context map
     * parameter must only contain keys and values of type String.
     *
     * @param contextMap must contain only keys and values of type String
     *
     * @since 1.5.1
     */
     void setContextMap(Map<String, Object> contextMap);
}
