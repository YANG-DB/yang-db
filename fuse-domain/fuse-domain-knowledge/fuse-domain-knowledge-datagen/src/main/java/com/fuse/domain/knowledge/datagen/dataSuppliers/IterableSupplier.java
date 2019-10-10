package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 *
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import javaslang.collection.Stream;

import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class IterableSupplier<T> extends RandomDataSupplier<Iterable<T>> {
    //region Constructors
    public IterableSupplier(Supplier<T> itemSupplier, Supplier<Integer> numItemsSupplier) {
        this(itemSupplier, numItemsSupplier, 0);
    }

    public IterableSupplier(Supplier<T> itemSupplier, Supplier<Integer> numItemsSupplier, long seed) {
        super(seed);
        this.itemSupplier = itemSupplier;
        this.numItemsSupplier = numItemsSupplier;
    }
    //endregion

    //region RandomDataSupplier<Iterable<String>> Implementation
    @Override
    public Iterable<T> get() {
        return Stream.fill(this.numItemsSupplier.get(), this.itemSupplier).toJavaList();
    }
    //endregion

    //region Fields
    private Supplier<T> itemSupplier;
    private Supplier<Integer> numItemsSupplier;
    //endregion
}
