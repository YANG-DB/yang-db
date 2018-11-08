package org.unipop.process.bulk;

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
