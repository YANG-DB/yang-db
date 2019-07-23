package org.unipop.util.iterator;

/*-
 * #%L
 * FlatMapIterator.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import java.util.Iterator;
import java.util.function.Function;

public class FlatMapIterator<TIn, TOut> implements Iterator<TOut> {
    //region Constructors
    public FlatMapIterator(Iterator<? extends TIn> inputs, Function<? super TIn, ? extends Iterable<? extends TOut>> mapper) {
        this.inputs = inputs;
        this.mapper = mapper;
    }
    //endregion

    //region Iterator Implementation
    @Override
    public boolean hasNext() {
        boolean currentHasNext;
        while (!(currentHasNext = current.hasNext()) && inputs.hasNext()) {
            current = mapper.apply(inputs.next()).iterator();
        }
        return currentHasNext;
    }

    @Override
    public TOut next() {
        return current.next();
    }
    //endregion

    //region Fields
    final Iterator<? extends TIn> inputs;
    java.util.Iterator<? extends TOut> current = java.util.Collections.emptyIterator();
    Function<? super TIn, ? extends Iterable<? extends TOut>> mapper;
    //endregion
}
