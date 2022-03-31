package com.yangdb.logging.slf4j;

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

import com.yangdb.logging.slf4j.api.ExtendedMDCAdapter;
import com.yangdb.logging.slf4j.api.NOPExtendedMDCAdapter;
import org.slf4j.MDC;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticExtendedMDCBinder;
import org.slf4j.impl.StaticMDCBinder;
import org.slf4j.spi.MDCAdapter;

import java.io.Closeable;
import java.util.Map;
import java.util.function.Supplier;

public class ExtendedMDC {

    static final String NULL_MDCA_URL = "http://www.slf4j.org/codes.html#null_MDCA";
    static ExtendedMDCAdapter extendedMDCAdapter;

    /**
     * An adapter to remove the key when done.
     */
    public static class ExtendedMDCCloseable implements Closeable {
        private final String key;

        private ExtendedMDCCloseable(String key) {
            this.key = key;
        }

        public void close() {
            ExtendedMDC.remove(this.key);
        }
    }

    private ExtendedMDC() {
    }

    /**
     * As of SLF4J version 1.7.14, StaticMDCBinder classes shipping in various bindings
     * come with a getSingleton() method. Previously only a public field called SINGLETON
     * was available.
     *
     * @return MDCAdapter
     * @throws NoClassDefFoundError in case no binding is available
     * @since 1.7.14
     */
    private static ExtendedMDCAdapter bwCompatibleGetExtendedMDCAdapterFromBinder() throws NoClassDefFoundError {
        try {
            return StaticExtendedMDCBinder.getSingleton().getEMDCA();
        } catch (NoSuchMethodError nsme) {
            // binding is probably a version of SLF4J older than 1.7.14
            return StaticExtendedMDCBinder.SINGLETON.getEMDCA();
        }
    }

    static {
        try {
            extendedMDCAdapter = bwCompatibleGetExtendedMDCAdapterFromBinder();
        } catch (NoClassDefFoundError ncde) {
            extendedMDCAdapter = new NOPExtendedMDCAdapter();
            String msg = ncde.getMessage();
            if (msg != null && msg.contains("StaticExtendedMDCBinder")) {
                Util.report("Failed to load class \"org.slf4j.impl.StaticExtendedMDCBinder\".");
                Util.report("Defaulting to no-operation ExtendedMDCAdapter implementation.");
            } else {
                throw ncde;
            }
        } catch (Exception e) {
            // we should never get here
            Util.report("ExtendedMDC binding unsuccessful.", e);
        }
    }

    /**
     * Put a diagnostic context value (the <code>val</code> parameter) as identified with the
     * <code>key</code> parameter into the current thread's diagnostic context map. The
     * <code>key</code> parameter cannot be null. The <code>val</code> parameter
     * can be null only if the underlying implementation supports it.
     *
     * <p>
     * This method delegates all work to the MDC of the underlying logging system.
     *
     * @param key non-null key
     * @param val value to put in the map
     *
     * @throws IllegalArgumentException
     *           in case the "key" parameter is null
     */
    public static void put(String key, Object val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        if (extendedMDCAdapter == null) {
            throw new IllegalStateException("extendedMDCAdapter cannot be null. See also " + NULL_MDCA_URL);
        }
        extendedMDCAdapter.put(key, val);
    }

    /**
     * Put a diagnostic context value (the <code>val</code> parameter) as identified with the
     * <code>key</code> parameter into the current thread's diagnostic context map. The
     * <code>key</code> parameter cannot be null. The <code>val</code> parameter
     * can be null only if the underlying implementation supports it.
     *
     * <p>
     * This method delegates all work to the MDC of the underlying logging system.
     * <p>
     * This method return a <code>Closeable</code> object who can remove <code>key</code> when
     * <code>close</code> is called.
     *
     * <p>
     * Useful with Java 7 for example :
     * <code>
     *   try(MDC.MDCCloseable closeable = MDC.putCloseable(key, value)) {
     *     ....
     *   }
     * </code>
     *
     * @param key non-null key
     * @param val value to put in the map
     * @return a <code>Closeable</code> who can remove <code>key</code> when <code>close</code>
     * is called.
     *
     * @throws IllegalArgumentException
     *           in case the "key" parameter is null
     */
    public static ExtendedMDCCloseable putCloseable(String key, Object val) throws IllegalArgumentException {
        put(key, val);
        return new ExtendedMDCCloseable(key);
    }

    /**
     * Get the diagnostic context identified by the <code>key</code> parameter. The
     * <code>key</code> parameter cannot be null.
     *
     * <p>
     * This method delegates all work to the MDC of the underlying logging system.
     *
     * @param key
     * @return the value identified by the <code>key</code> parameter.
     * @throws IllegalArgumentException
     *           in case the "key" parameter is null
     */
    public static <V> V get(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }

        if (extendedMDCAdapter == null) {
            throw new IllegalStateException("extendedMDCAdapter cannot be null. See also " + NULL_MDCA_URL);
        }
        return extendedMDCAdapter.get(key);
    }

    public static <V> V get(String key, V defaultValue) throws IllegalArgumentException {
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    public static <V> V get(String key, Supplier<V> defaultValueSupplier) throws IllegalArgumentException {
        V value = get(key);
        return value == null ? defaultValueSupplier.get() : value;
    }

    /**
     * Remove the diagnostic context identified by the <code>key</code> parameter using
     * the underlying system's MDC implementation. The <code>key</code> parameter
     * cannot be null. This method does nothing if there is no previous value
     * associated with <code>key</code>.
     *
     * @param key
     * @throws IllegalArgumentException
     *           in case the "key" parameter is null
     */
    public static void remove(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }

        if (extendedMDCAdapter == null) {
            throw new IllegalStateException("extendedMDCAdapter cannot be null. See also " + NULL_MDCA_URL);
        }
        extendedMDCAdapter.remove(key);
    }

    /**
     * Clear all entries in the MDC of the underlying implementation.
     */
    public static void clear() {
        if (extendedMDCAdapter == null) {
            throw new IllegalStateException("extendedMDCAdapter cannot be null. See also " + NULL_MDCA_URL);
        }
        extendedMDCAdapter.clear();
    }

    /**
     * Return a copy of the current thread's context map, with keys and values.
     * Returned value may be null.
     *
     * @return A copy of the current thread's context map. May be null.
     * @since 1.5.1
     */
    public static Map<String, Object> getCopyOfContextMap() {
        if (extendedMDCAdapter == null) {
            throw new IllegalStateException("extendedMDCAdapter cannot be null. See also " + NULL_MDCA_URL);
        }
        return extendedMDCAdapter.getCopyOfContextMap();
    }

    /**
     * Set the current thread's context map by first clearing any existing map and
     * then copying the map passed as parameter. The context map passed as
     * parameter must only contain keys and values of type Object.
     *
     * @param contextMap
     *          must contain only keys and values of type Object
     * @since 1.5.1
     */
    public static void setContextMap(Map<String, Object> contextMap) {
        if (extendedMDCAdapter == null) {
            throw new IllegalStateException("extendedMDCAdapter cannot be null. See also " + NULL_MDCA_URL);
        }
        extendedMDCAdapter.setContextMap(contextMap);
    }

    /**
     * Returns the MDCAdapter instance currently in use.
     *
     * @return the MDcAdapter instance currently in use.
     * @since 1.4.2
     */
    public static ExtendedMDCAdapter getMDCAdapter() {
        return extendedMDCAdapter;
    }

}
