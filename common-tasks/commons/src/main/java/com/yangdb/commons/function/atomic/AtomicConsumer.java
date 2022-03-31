package com.yangdb.commons.function.atomic;

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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class AtomicConsumer<T> implements Consumer<T> {
    //region Constructors
    public AtomicConsumer(Consumer<T> consumer) {
        this(consumer, new AtomicBoolean(false));
    }

    public AtomicConsumer(Consumer<T> consumer, AtomicBoolean atomicBoolean) {
        this.consumer = consumer;
        this.atomicBoolean = atomicBoolean;
        this.atomicBoolean.set(false);
    }
    //endregion

    //region Consumer Implementation
    @Override
    public void accept(T t) {
        try {
            while (!this.atomicBoolean.compareAndSet(false, true)) {
                LockSupport.parkNanos(0L);
            }

            this.consumer.accept(t);
        } finally {
            this.atomicBoolean.set(false);
        }
    }
    //endregion

    //region Fields
    private final Consumer<T> consumer;
    private final AtomicBoolean atomicBoolean;
    //endregion
}
