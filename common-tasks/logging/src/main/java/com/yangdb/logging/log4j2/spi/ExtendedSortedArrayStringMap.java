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
import org.apache.logging.log4j.util.SortedArrayStringMap;

import java.util.Map;

public class ExtendedSortedArrayStringMap extends SortedArrayStringMap implements ExtendedStringMap {
    //region Constructors
    public ExtendedSortedArrayStringMap() {
        super();
    }

    public ExtendedSortedArrayStringMap(final int initialCapacity) {
        super(initialCapacity);
    }

    public ExtendedSortedArrayStringMap(final ReadOnlyStringMap other) {
        super(other);
    }

    public ExtendedSortedArrayStringMap(final Map<String, ?> map) {
        super(map);
    }
    //endregion
}
