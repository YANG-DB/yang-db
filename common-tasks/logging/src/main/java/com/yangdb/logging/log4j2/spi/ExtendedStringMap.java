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

import com.yangdb.logging.slf4j.api.ExtendedMDCItem;
import org.apache.logging.log4j.util.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface ExtendedStringMap extends IndexedStringMap {
    /**
     * Returns a non-{@code null} mutable {@code Map<String, Object>} containing a snapshot of this data structure.
     *
     * @return a mutable copy of this data structure in {@code Map<String, Object>} form.
     */
    default Map<String, Object> toObjectMap() {
        final Map<String, Object> result = new HashMap<>(this.size());
        for (int i = 0; i < this.size(); i++) {
            Object value = this.getValueAt(i);
            if (value instanceof ExtendedMDCItem) {
                value = ((ExtendedMDCItem<?>) value).copy();
            }
            result.put(getKeyAt(i), value);
        }
        return result;
    }

    class Empty implements ExtendedStringMap {
        public static Empty instance = new Empty();


        //region ExtendedStringMap Implementation
        @Override
        public Map<String, Object> toObjectMap() {
            return Collections.emptyMap();
        }

        @Override
        public void clear() {

        }

        @Override
        public void freeze() {

        }

        @Override
        public boolean isFrozen() {
            return false;
        }

        @Override
        public void putAll(ReadOnlyStringMap source) {

        }

        @Override
        public void putValue(String key, Object value) {

        }

        @Override
        public void remove(String key) {

        }

        @Override
        public Map<String, String> toMap() {
            return Collections.emptyMap();
        }

        @Override
        public boolean containsKey(String key) {
            return false;
        }

        @Override
        public <V> void forEach(BiConsumer<String, ? super V> action) {

        }

        @Override
        public <V, S> void forEach(TriConsumer<String, ? super V, S> action, S state) {

        }

        @Override
        public <V> V getValue(String key) {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public String getKeyAt(int index) {
            return null;
        }

        @Override
        public <V> V getValueAt(int index) {
            return null;
        }

        @Override
        public int indexOfKey(String key) {
            return 0;
        }
        //endregion
    }
}
