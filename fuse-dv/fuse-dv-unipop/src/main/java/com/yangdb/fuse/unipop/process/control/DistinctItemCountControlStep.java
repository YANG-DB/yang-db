package com.yangdb.fuse.unipop.process.control;

/*-
 * #%L
 * fuse-dv-unipop
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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class DistinctItemCountControlStep<S, T> extends AbstractStep<S, S> {
    //region Constructors
    public DistinctItemCountControlStep(
            Traversal.Admin traversal,
            Function<S, T> itemValueFunction,
            Supplier<Set<T>> itemsSupplier,
            Supplier<Integer> maxCountSupplier) {
        super(traversal);
        this.itemValueFunction = itemValueFunction;
        this.itemsSupplier = itemsSupplier;
        this.maxCountSupplier = maxCountSupplier;
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<S> processNextStart() throws NoSuchElementException {
        if (this.itemsSupplier.get().size() >= this.maxCountSupplier.get()) {
            throw FastNoSuchElementException.instance();
        }

        Traverser.Admin<S> nextTraverser = this.starts.next();
        T itemValue = this.itemValueFunction.apply(nextTraverser.get());
        this.itemsSupplier.get().add(itemValue);

        return nextTraverser;
    }
    //endregion

    //region Fields
    private Function<S, T> itemValueFunction;
    private Supplier<Set<T>> itemsSupplier;
    private Supplier<Integer> maxCountSupplier;
    //endregion
}
