package com.yangdb.fuse.unipop.controller.utils.map;

/*-
 *
 * fuse-dv-unipop
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

        if (map != null) {
            this.map.putAll(map);
        }
    }
    //endregion

    //region Public Methods
    public MapBuilder<TKey, TValue> put(TKey key, TValue value) {
        this.map.put(key, value);
        return this;
    }

    public MapBuilder<TKey, TValue> putAll(Map<TKey, TValue> map) {
        this.map.putAll(map);
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
