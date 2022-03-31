package com.yangdb.commons.allocation;

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

import java.util.concurrent.atomic.AtomicLong;

public class InMemorySequenceLongIdAllocator  implements LongIdAllocator {
    //region Constructors
    public InMemorySequenceLongIdAllocator() {
        this(0L);
    }

    public InMemorySequenceLongIdAllocator(long start) {
        this.counter = start;
    }
    //endregion

    //region LongIdAllocator
    @Override
    public long allocate() {
        return this.counter++;
    }
    //endregion

    //region Fields
    private long counter;
    //endregion

    public static class Atomic implements LongIdAllocator {
        //region Constructors
        public Atomic() {
            this(0L);
        }

        public Atomic(long start) {
            this.counter = new AtomicLong(start);
        }
        //endregion

        //region LongIdAllocator
        @Override
        public long allocate() {
            return this.counter.getAndIncrement();
        }
        //endregion

        //region Fields
        private AtomicLong counter;
        //endregion
    }
}
