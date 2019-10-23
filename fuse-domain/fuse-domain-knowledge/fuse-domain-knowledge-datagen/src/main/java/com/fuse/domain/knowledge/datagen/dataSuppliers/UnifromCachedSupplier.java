package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import javaslang.collection.Stream;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class UnifromCachedSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public UnifromCachedSupplier(Supplier<T> supplier, int maxCacheSize) {
        this(supplier, maxCacheSize, 0);
    }

    public UnifromCachedSupplier(Supplier<T> supplier, int maxCacheSize, long seed) {
        super(seed);
        this.supplier = supplier;
        this.maxCacheSize = maxCacheSize;
        this.cacheSet = new HashSet<>();
    }

    public UnifromCachedSupplier(Iterable<T> cache) {
        this(cache, 0);
    }

    public UnifromCachedSupplier(Iterable<T> cache, long seed) {
        super(seed);
        this.cacheSet = Stream.ofAll(cache).toJavaSet();
        this.maxCacheSize = this.cacheSet.size();
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public T get() {
        if (this.cacheSet.size() == maxCacheSize) {
            this.cacheList = Stream.ofAll(this.cacheSet).toJavaList();
            this.cacheSet = null;
        }

        if (this.cacheList != null) {
            return this.cacheList.get(this.random.nextInt(this.cacheList.size()));
        }

        T item = this.supplier.get();
        this.cacheSet.add(item);

        return item;
    }
    //endregion

    //region Fields
    private Supplier<T> supplier;
    private List<T> cacheList;
    private Set<T> cacheSet;

    private int maxCacheSize;
    //endregion
}
