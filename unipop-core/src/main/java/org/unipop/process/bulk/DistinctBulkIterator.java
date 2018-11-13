package org.unipop.process.bulk;

/*-
 * #%L
 * DistinctBulkIterator.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by roman.margolis on 13/03/2018.
 */
public class DistinctBulkIterator<S, TId> implements Iterator<List<S>> {
    //region Constructors
    public DistinctBulkIterator(Iterator<S> innerIterator, Function<S, TId> idFunction, Supplier<Supplier<Integer>> bulkSizeSupplierFactory) {
        this.innerIterator = innerIterator;

        this.idFunction = idFunction;
        this.bulkSizeSupplierFactory = bulkSizeSupplierFactory;
        this.nextBulk = Collections.emptyList();
    }
    //endregion

    //region Iterator Implementation
    @Override
    public boolean hasNext() {
        if (this.nextBulk.isEmpty()) {
            this.nextBulk = getNextBulk();
        }

        return !this.nextBulk.isEmpty();
    }

    @Override
    public List<S> next() {
        if (this.nextBulk.isEmpty()) {
            this.nextBulk = getNextBulk();
        }

        if (this.nextBulk.isEmpty()) {
            throw FastNoSuchElementException.instance();
        }

        List<S> bulk = this.nextBulk;
        this.nextBulk = Collections.emptyList();
        return bulk;
    }
    //endregion

    //region Private Methods
    private List<S> getNextBulk() {
        Supplier<Integer> bulkSizeSupplier = bulkSizeSupplierFactory.get();

        List<S> bulk = new ArrayList<>(bulkSizeSupplier.get());
        HashSet<TId> idSet = new HashSet<>();
        try {
            while(idSet.size() < bulkSizeSupplier.get()) {
                S s = this.innerIterator.next();
                bulk.add(s);
                idSet.add(this.idFunction.apply(s));
            }
        } catch (FastNoSuchElementException ex) {

        }

        if (bulk.isEmpty()) {
           return Collections.emptyList();
        }

        return bulk;
    }
    //endregion

    //region Fields
    private Iterator<S> innerIterator;
    private Supplier<Supplier<Integer>> bulkSizeSupplierFactory;

    private Function<S, TId> idFunction;

    private List<S> nextBulk;
    //endregion
}
