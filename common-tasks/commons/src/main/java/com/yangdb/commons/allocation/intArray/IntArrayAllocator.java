package com.yangdb.commons.allocation.intArray;

/*-
 * #%L
 * commons
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

import org.apache.commons.lang3.ArrayUtils;

public interface IntArrayAllocator {
    int[] allocate(int length);
    int[] allocate(int length, int initValue);

    class Empty implements IntArrayAllocator {
        public static Empty instance = new Empty();

        //region IntArrayAllocator Implementation
        @Override
        public int[] allocate(int length) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] allocate(int length, int initValue) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        //endregion
    }
}
