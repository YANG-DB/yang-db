package org.unipop.process.bulk;

import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by sbarzilay on 7/6/16.
 */
public class BulkIterator<S> implements Iterator<List<S>> {
    //region Constructors
    public BulkIterator(Iterator<S> innerIterator, Supplier<Supplier<Integer>> bulkSizeSupplierFactory) {
        this.innerIterator = innerIterator;
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
        try {
            for (int i = 0; i < bulkSizeSupplier.get(); i++) {
                bulk.add(this.innerIterator.next());
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

    private List<S> nextBulk;
    //endregion
}
