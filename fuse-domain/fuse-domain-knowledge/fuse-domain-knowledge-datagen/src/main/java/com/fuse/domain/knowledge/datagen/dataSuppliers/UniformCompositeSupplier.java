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

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/24/2018.
 */
public class UniformCompositeSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public UniformCompositeSupplier(Iterable<Supplier<T>> suppliers) {
        this(suppliers, 0);
    }

    public UniformCompositeSupplier(Iterable<Supplier<T>> suppliers, long seed) {
        super(seed);
        this.suppliers = Stream.ofAll(suppliers).toJavaList();
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public T get() {
        return this.suppliers.get(this.random.nextInt(this.suppliers.size())).get();
    }
    //endregion

    //region Fields
    private List<Supplier<T>> suppliers;
    //endregion
}
