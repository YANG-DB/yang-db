package com.yangdb.logging.log4j2;

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

import com.yangdb.logging.log4j2.spi.ExtendedGarbageFreeSortedArrayThreadContextMap;
import com.yangdb.logging.log4j2.spi.ExtendedThreadContextMap;
import com.yangdb.logging.log4j2.spi.ReadOnlyExtendedThreadContextMap;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextMapFactory;

import java.util.*;

public class ExtendedThreadContext {
    public static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    private static ExtendedThreadContextMap contextMap;

    static {
        init();
    }

    private ExtendedThreadContext() { }

    /**
     * <em>Consider private, used for testing.</em>
     */
    static void init() {
        contextMap = new ExtendedGarbageFreeSortedArrayThreadContextMap();
    }

    /**
     * Puts a context value (the <code>value</code> parameter) as identified with the <code>key</code> parameter into
     * the current thread's context map.
     *
     * <p>
     * If the current thread does not have a context map it is created as a side effect.
     * </p>
     *
     * @param key The key name.
     * @param value The key value.
     */
    public static void put(final String key, final Object value) {
        contextMap.put(key, value);
    }

    /**
     * Puts all given context map entries into the current thread's
     * context map.
     *
     * <p>If the current thread does not have a context map it is
     * created as a side effect.</p>
     * @param m The map.
     * @since 2.7
     */
    public static void putAll(final Map<String, Object> m) {
        contextMap.putAll(m);
    }

    /**
     * Gets the context value identified by the <code>key</code> parameter.
     *
     * <p>
     * This method has no side effects.
     * </p>
     *
     * @param key The key to locate.
     * @return The value associated with the key or null.
     */
    public static <V> V get(final String key) {
        return contextMap.get(key);
    }

    /**
     * Removes the context value identified by the <code>key</code> parameter.
     *
     * @param key The key to remove.
     */
    public static void remove(final String key) {
        contextMap.remove(key);
    }

    /**
     * Removes the context values identified by the <code>keys</code> parameter.
     *
     * @param keys The keys to remove.
     *
     * @since 2.8
     */
    public static void removeAll(final Iterable<String> keys) {
        contextMap.removeAll(keys);
    }

    /**
     * Clears the context map.
     */
    public static void clearMap() {
        contextMap.clear();
    }

    /**
     * Clears the context map and stack.
     */
    public static void clearAll() {
        clearMap();
    }

    /**
     * Determines if the key is in the context.
     *
     * @param key The key to locate.
     * @return True if the key is in the context, false otherwise.
     */
    public static boolean containsKey(final String key) {
        return contextMap.containsKey(key);
    }

    /**
     * Returns a mutable copy of current thread's context Map.
     *
     * @return a mutable copy of the context.
     */
    public static Map<String, Object> getContext() {
        return contextMap.getCopy();
    }

    /**
     * Returns an immutable view of the current thread's context Map.
     *
     * @return An immutable view of the ThreadContext Map.
     */
    public static Map<String, Object> getImmutableContext() {
        final Map<String, Object> map = contextMap.getImmutableMapOrNull();
        return map == null ? EMPTY_MAP : map;
    }

    /**
     * Returns a read-only view of the internal data structure used to store thread context key-value pairs,
     * or {@code null} if the internal data structure does not implement the
     * {@code ReadOnlyThreadContextMap} interface.
     * <p>
     * The {@link DefaultThreadContextMap} implementation does not implement {@code ReadOnlyThreadContextMap}, so by
     * default this method returns {@code null}.
     * </p>
     *
     * @return the internal data structure used to store thread context key-value pairs or {@code null}
     * @since 2.8
     */
    public static ReadOnlyExtendedThreadContextMap getExtendedThreadContextMap() {
        return contextMap;
    }

    /**
     * Returns true if the Map is empty.
     *
     * @return true if the Map is empty, false otherwise.
     */
    public static boolean isEmpty() {
        return contextMap.isEmpty();
    }
}
