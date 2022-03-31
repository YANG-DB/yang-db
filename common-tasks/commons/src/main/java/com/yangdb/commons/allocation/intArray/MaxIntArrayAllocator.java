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

import java.util.Arrays;

public class MaxIntArrayAllocator implements IntArrayAllocator {
    //region Constructors
    public MaxIntArrayAllocator() {
        this(-1);
    }

    public MaxIntArrayAllocator(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }
    //endregion

    //region IntArrayAllocator Implementation
    @Override
    public int[] allocate(int length) {
        if (this.sizeLimit < 0 || this.sizeLimit > length) {
            if (this.buffer == null || this.buffer.length < length) {
                this.buffer = new int[length];
            }
        } else {
            return new int[length];
        }

        return this.buffer;
    }

    @Override
    public int[] allocate(int length, int initValue) {
        if (this.sizeLimit < 0 || this.sizeLimit > length) {
            if (this.buffer == null || this.buffer.length < length) {
                this.buffer = new int[length];
            }

            if (initValue != 0) {
                Arrays.fill(this.buffer, 0, length, initValue);
            }

            return this.buffer;
        } else if (initValue != 0) {
            int[] buffer = new int[length];
            Arrays.fill(buffer, 0, length, initValue);
            return buffer;
        } else {
            return new int[length];
        }
    }
    //endregion

    //region Fields
    private int[] buffer;
    private final int sizeLimit;
    //endregion
}
