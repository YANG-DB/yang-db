package com.kayhut.fuse.unipop.process.control;

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
