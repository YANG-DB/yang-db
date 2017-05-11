package com.kayhut.fuse.unipop.controller.utils.map;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Roman on 5/9/2017.
 */
public class MapBuilder<TKey, TValue> implements Supplier<Map<TKey, TValue>> {
    //region Constructors
    public MapBuilder() {
        this.map = new HashMap<>();
    }

    public MapBuilder(Map<TKey, TValue> map) {
        this();
        this.map.putAll(map);
    }
    //endregion

    //region Public Methods
    public MapBuilder<TKey, TValue> put(TKey key, TValue value) {
        this.map.put(key, value);
        return this;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Map<TKey, TValue> get() {
        return this.map;
    }
    //endregion

    //region Fields
    private Map<TKey, TValue> map;
    //endregion
}
