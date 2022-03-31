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

import com.yangdb.commons.builders.GenericBuilder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.yangdb.commons.util.GenericUtils.infere;

public class InMemorySequenceIntIdAllocator implements IntIdAllocator {
    //region Constructors
    public InMemorySequenceIntIdAllocator() {
        this(0);
    }

    public InMemorySequenceIntIdAllocator(int start) {
        this.counter = start;
    }
    //endregion

    //region IntIdAllocator
    @Override
    public int allocate() {
        return this.counter++;
    }
    //endregion

    //region Fields
    private int counter;
    //endregion

    public static class Atomic implements IntIdAllocator {
        //region Constructors
        public Atomic() {
            this(0);
        }

        public Atomic(int start) {
            this.counter = new AtomicInteger(start);
        }
        //endregion

        //region LongIdAllocator
        @Override
        public int allocate() {
            return this.counter.getAndIncrement();
        }
        //endregion

        //region Fields
        private final AtomicInteger counter;
        //endregion
    }

    public static class Bounded implements IntIdAllocator {
        //region Constructors
        public Bounded(int start, int limit, Supplier<RuntimeException> limitExceptionSupplier) {
            this.counter = start;
            this.limit = limit;
            this.limitExceptionSupplier = limitExceptionSupplier;
        }
        //endregion

        //region IntIdAllocator Implementation
        @Override
        public int allocate() {
            if (this.counter < this.limit) {
                return this.counter++;
            } else {
                throw this.limitExceptionSupplier.get();
            }
        }
        //endregion

        //region Fields
        private int counter;
        private int limit;
        private Supplier<RuntimeException> limitExceptionSupplier;
        //endregion

        public static class Builder implements GenericBuilder<Bounded> {
            //region Constructors
            public Builder() {
                this.allocator = new Bounded(0, Integer.MAX_VALUE, null);
            }
            //endregion

            //region Builder Implementation
            public Builder start(int start) {
                this.allocator.counter = start;
                return this;
            }

            public Builder limit(int limit) {
                this.allocator.limit = limit;
                return this;
            }

            public Builder limitExceptionSupplier(Supplier<RuntimeException> limitExceptionSupplier) {
                this.allocator.limitExceptionSupplier = limitExceptionSupplier;
                return this;
            }

            @Override
            public <T2 extends Bounded> T2 build() {
                if (this.allocator.limitExceptionSupplier == null) {
                    this.allocator.limitExceptionSupplier = () -> new RuntimeException("allocator has reached it limit of " + this.allocator.limit);
                }

                return infere(new Bounded(this.allocator.counter, this.allocator.limit, this.allocator.limitExceptionSupplier));
            }
            //endregion

            //region Fields
            private final Bounded allocator;
            //endregion
        }

        public static class Atomic implements IntIdAllocator {
            //region Constructors
            public Atomic(int start, int limit, Supplier<RuntimeException> limitExceptionSupplier) {
                this.counter = new AtomicInteger(start);
                this.limit = limit;
                this.limitExceptionSupplier = limitExceptionSupplier;
            }
            //endregion

            //region IntIdAllocator Implementation
            @Override
            public int allocate() {
                if (!this.limitReached) {
                    int counterValue = this.counter.getAndIncrement();
                    if (counterValue < this.limit) {
                        return counterValue;
                    } else {
                        this.limitReached = true;
                        throw this.limitExceptionSupplier.get();
                    }
                } else {
                    throw this.limitExceptionSupplier.get();
                }
            }
            //endregion

            //region Fields
            private AtomicInteger counter;
            private int limit;
            private boolean limitReached;
            private Supplier<RuntimeException> limitExceptionSupplier;
            //endregion

            public static class Builder implements GenericBuilder<Atomic> {
                //region Constructors
                public Builder() {
                    this.allocator = new Atomic(0, Integer.MAX_VALUE, null);
                }
                //endregion

                //region Builder Implementation
                public Builder start(int start) {
                    this.allocator.counter.set(start);
                    return this;
                }

                public Builder limit(int limit) {
                    this.allocator.limit = limit;
                    return this;
                }

                public Builder limitExceptionSupplier(Supplier<RuntimeException> limitExceptionSupplier) {
                    this.allocator.limitExceptionSupplier = limitExceptionSupplier;
                    return this;
                }

                @Override
                public <T2 extends Atomic> T2 build() {
                    if (this.allocator.limitExceptionSupplier == null) {
                        this.allocator.limitExceptionSupplier = () -> new RuntimeException("allocator has reached it limit of " + this.allocator.limit);
                    }

                    return infere(new Atomic(this.allocator.counter.get(), this.allocator.limit, this.allocator.limitExceptionSupplier));
                }
                //endregion

                //region Fields
                private final Atomic allocator;
                //endregion
            }
        }
    }
}
