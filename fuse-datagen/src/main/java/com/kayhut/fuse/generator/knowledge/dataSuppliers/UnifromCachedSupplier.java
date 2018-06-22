package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class CachedSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public CachedSupplier(Supplier<T> supplier, int maxCacheSize) {
        this(supplier, maxCacheSize, 0);
    }

    public CachedSupplier(Supplier<T> supplier, int maxCacheSize, long seed) {
        super(seed);
        this.supplier = supplier;
        this.maxCacheSize = maxCacheSize;
        this.cacheSet = new HashSet<>();
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public T get() {

    }
    //endregion

    //region Fields
    private Supplier<T> supplier;
    private List<T> cacheList;
    private Set<T> cacheSet;

    private int maxCacheSize;
    //endregion
}
