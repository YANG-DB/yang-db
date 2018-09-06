package com.fuse.domain.knowledge.datagen.dataSuppliers;

import javaslang.collection.Stream;

import java.util.*;
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
        this.cacheList = new ArrayList<>();
    }

    public UnifromCachedSupplier(Iterable<T> cache) {
        this(cache, 0);
    }

    public UnifromCachedSupplier(Iterable<T> cache, long seed) {
        super(seed);
        this.cacheList = Stream.ofAll(cache).toJavaList();
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public T get() {
        if (this.cacheSet != null) {
            if (this.cacheSet.size() == maxCacheSize) {
                this.cacheSet = null;
            }
        }

        if (this.cacheList != null && this.cacheSet == null) {
            if (this.cacheList.isEmpty()) {
                throw new NoSuchElementException();
            }
            return this.cacheList.get(this.random.nextInt(this.cacheList.size()));
        }

        T item = this.supplier.get();
        this.cacheSet.add(item);
        this.cacheList.add(item);

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
