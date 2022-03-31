package com.yangdb.commons.function.supplier;

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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.yangdb.commons.util.GenericUtils.infere;

public class CyclicSupplier<T> implements Supplier<T> {
    //region Constructors
    public CyclicSupplier(List<T> items) {
        this.items = infere(items.toArray());
    }

    @SafeVarargs
    public CyclicSupplier(T...items) {
        this.items = items;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public T get() {
        try {
            return this.items[this.currentIndex++];
        } finally {
            if (this.currentIndex == this.items.length) {
                this.currentIndex = 0;
            }
        }
    }
    //endregion

    //region Fields
    private final T[] items;
    private int currentIndex;
    //endregion

    public static class Atomic<T> implements Supplier<T> {
        //region Constructors
        public Atomic(List<T> items) {
            this.items = infere(items.toArray());
            this.currentIndex = new AtomicInteger(0);
        }

        @SafeVarargs
        public Atomic(T...items) {
            this.items = items;
            this.currentIndex = new AtomicInteger(0);
        }
        //endregion

        //region Supplier Implementation
        @Override
        public T get() {
            if (this.currentIndex.compareAndSet(this.items.length, 0)) {
                return this.items[0];
            } else {
                int index = this.currentIndex.getAndIncrement();
                if (index >= this.items.length) {
                    index %= this.items.length;
                }
                return this.items[index];
            }
        }
        //endregion

        //region Fields
        private final T[] items;
        private final AtomicInteger currentIndex;
        //endregion
    }
}
