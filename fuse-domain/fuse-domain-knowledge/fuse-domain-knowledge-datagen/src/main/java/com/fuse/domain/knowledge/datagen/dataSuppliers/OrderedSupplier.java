package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class OrderedSupplier<T> implements Supplier<T> {
    //region Constructors
    public OrderedSupplier(Iterable<T> items) {
        this.iterator = items.iterator();
    }
    //endregion

    //region Supplier Implementation
    @Override
    public T get() {
        return iterator.next();
    }
    //endregion

    //region
    private Iterator<T> iterator;
    //endregion
}
