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
import java.util.List;

public class CompositeIntArrayAllocator implements IntArrayAllocator {
    //region Constructors
    public CompositeIntArrayAllocator(IntArrayAllocator...allocators) {
        this(Arrays.asList(allocators));
    }

    public CompositeIntArrayAllocator(List<IntArrayAllocator> allocators) {
        this.allocators = allocators;
    }
    //endregion

    //region IntArrayAllocator Implementation
    @Override
    public int[] allocate(int length) {
        try {
            return this.allocators.get(this.currentAllocator++).allocate(length);
        } finally {
            if (this.currentAllocator == this.allocators.size()) {
                this.currentAllocator = 0;
            }
        }
    }

    @Override
    public int[] allocate(int length, int initValue) {
        try {
            return this.allocators.get(this.currentAllocator++).allocate(length, initValue);
        } finally {
            if (this.currentAllocator == this.allocators.size()) {
                this.currentAllocator = 0;
            }
        }
    }
    //endregion

    //region Fields
    private final List<IntArrayAllocator> allocators;
    private int currentAllocator;
    //endregion
}
