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

import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExtendedGarbageFreeSortedArrayThreadContextMap implements ExtendedThreadContextMap {
    /**
     * Property name ({@value} ) for selecting {@code InheritableThreadLocal} (value "true") or plain
     * {@code ThreadLocal} (value is not "true") in the implementation.
     */
    public static final String INHERITABLE_MAP = "isThreadContextMapInheritable";

    /**
     * The default initial capacity.
     */
    protected static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * System property name that can be used to control the data structure's initial capacity.
     */
    protected static final String PROPERTY_NAME_INITIAL_CAPACITY = "log4j2.ThreadContext.initial.capacity";

    protected final ThreadLocal<ExtendedStringMap> localMap;

    private static volatile int initialCapacity;
    private static volatile boolean inheritableMap;

    public ExtendedGarbageFreeSortedArrayThreadContextMap() {
        this.localMap = createThreadLocalMap();
    }

    // LOG4J2-479: by default, use a plain ThreadLocal, only use InheritableThreadLocal if configured.
    // (This method is package protected for JUnit tests.)
    private ThreadLocal<ExtendedStringMap> createThreadLocalMap() {
        if (inheritableMap) {
            return new InheritableThreadLocal<ExtendedStringMap>() {
                @Override
                protected ExtendedStringMap childValue(final ExtendedStringMap parentValue) {
                    return parentValue != null ? createStringMap(parentValue) : null;
                }
            };
        }
        // if not inheritable, return plain ThreadLocal with null as initial value
        return new ThreadLocal<>();
    }

    /**
     * Returns an implementation of the {@code StringMap} used to back this thread context map.
     * <p>
     * Subclasses may override.
     * </p>
     * @return an implementation of the {@code StringMap} used to back this thread context map
     */
    protected ExtendedStringMap createStringMap() {
        return new ExtendedSortedArrayStringMap(initialCapacity);
    }

    /**
     * Returns an implementation of the {@code StringMap} used to back this thread context map, pre-populated
     * with the contents of the specified context data.
     * <p>
     * Subclasses may override.
     * </p>
     * @param original the key-value pairs to initialize the returned context data with
     * @return an implementation of the {@code StringMap} used to back this thread context map
     */
    protected ExtendedStringMap createStringMap(final ReadOnlyStringMap original) {
        return new ExtendedSortedArrayStringMap(original);
    }

    private ExtendedStringMap getThreadLocalMap() {
        ExtendedStringMap map = localMap.get();
        if (map == null) {
            map = createStringMap();
            localMap.set(map);
        }
        return map;
    }

    @Override
    public void put(final String key, final Object value) {
        getThreadLocalMap().putValue(key, value);
    }

    @Override
    public void putAll(final Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        final StringMap map = getThreadLocalMap();
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            map.putValue(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public <V> V get(final String key) {
        return getThreadLocalMap().getValue(key);
    }

    @Override
    public void remove(final String key) {
        final StringMap map = localMap.get();
        if (map != null) {
            map.remove(key);
        }
    }

    @Override
    public void removeAll(final Iterable<String> keys) {
        final StringMap map = localMap.get();
        if (map != null) {
            for (final String key : keys) {
                map.remove(key);
            }
        }
    }

    @Override
    public void clear() {
        final StringMap map = localMap.get();
        if (map != null) {
            map.clear();
        }
    }

    @Override
    public boolean containsKey(final String key) {
        final StringMap map = localMap.get();
        return map != null && map.containsKey(key);
    }

    @Override
    public Map<String, Object> getCopy() {
        final ExtendedStringMap map = localMap.get();

        if (map == null) {
            return new HashMap<>();
        } else {
            return map.toObjectMap();
        }
    }

    @Override
    public Map<String, Object> getImmutableMapOrNull() {
        final ExtendedStringMap map = localMap.get();
        return map == null ? null : Collections.unmodifiableMap(map.toObjectMap());
    }

    @Override
    public ExtendedStringMap getReadOnlyContextData() {
        ExtendedStringMap map = localMap.get();
        if (map == null) {
            map = createStringMap();
            localMap.set(map);
        }
        return map;
    }

    @Override
    public boolean isEmpty() {
        final StringMap map = localMap.get();
        return map == null || map.size() == 0;
    }

    @Override
    public String toString() {
        final StringMap map = localMap.get();
        return map == null ? "{}" : map.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final StringMap map = this.localMap.get();
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExtendedThreadContextMap)) {
            return false;
        }
        final ExtendedThreadContextMap other = (ExtendedThreadContextMap) obj;
        final Map<String, Object> map = this.getImmutableMapOrNull();
        final Map<String, Object> otherMap = other.getImmutableMapOrNull();
        if (map == null) {
            if (otherMap != null) {
                return false;
            }
        } else if (!map.equals(otherMap)) {
            return false;
        }
        return true;
    }
}
