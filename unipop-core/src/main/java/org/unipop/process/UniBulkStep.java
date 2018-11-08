package org.unipop.process;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.util.iterator.EmptyIterator;
//import org.unipop.process.traversal.QueueExpandableStepIterator;
import org.unipop.common.valueSuppliers.CompiledSupplierFactory;
import org.unipop.process.bulk.BulkIterator;
import org.unipop.common.valueSuppliers.FixedValueSupplierFactory;
import org.unipop.common.valueSuppliers.LinearDecayingValueSupplierFactory;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public abstract class UniBulkStep<S, E> extends AbstractStep<S, E> {
    private static Supplier<Supplier<Integer>> cachedBulkSizeSupplierFactory = null;

    //region Constructors
    public UniBulkStep(Traversal.Admin traversal, UniGraph graph) {
        super(traversal);

        int maxBulkSize = graph.configuration().getInt("bulk.max", 100);
        int minBulkSize = graph.configuration().getInt("bulk.min", maxBulkSize);
        long decayInterval = graph.configuration().getLong("bulk.decayInterval", 200L);

        if (cachedBulkSizeSupplierFactory == null) {
            if (maxBulkSize == minBulkSize) {
                cachedBulkSizeSupplierFactory = new FixedValueSupplierFactory(maxBulkSize);
            } else {
                cachedBulkSizeSupplierFactory = new CompiledSupplierFactory(
                        new LinearDecayingValueSupplierFactory(maxBulkSize, minBulkSize, decayInterval),
                        decayInterval,
                        100,
                        CompiledSupplierFactory.ValueAggMethod.max);
            }
        }

        this.bulkSizeSupplierFactory = cachedBulkSizeSupplierFactory;

        this.results = EmptyIterator.instance();

        /*this.profiler = this.traversal.getSideEffects().exists("profiler") ?
                this.traversal.getSideEffects().get("profiler") :
                Profiler.Noop.instance;*/
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<E> processNextStart() throws NoSuchElementException {
        if (this.results.equals(EmptyIterator.instance())) {
            this.results = process();
        }

        return results.next();
    }

    @Override
    public void reset() {
        super.reset();
        this.results = EmptyIterator.instance();
    }
    //endregion

    //region Abstract Methods
    protected abstract Iterator<Traverser.Admin<E>> process(List<Traverser.Admin<S>> traversers);
    //endregion

    //region Private Methods
    protected Iterator<Traverser.Admin<E>> process() {
        return Stream.ofAll(() -> new BulkIterator<>(this.starts, this.bulkSizeSupplierFactory))
                .flatMap(bulk -> Stream.ofAll(() -> process(bulk))).iterator();
    }
    //endregion

    //region Fields
    protected Supplier<Supplier<Integer>> bulkSizeSupplierFactory;
    protected Iterator<Traverser.Admin<E>> results = null;

    protected Profiler profiler;
    //endregion
}
