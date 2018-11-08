package org.unipop.util.iterator;

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
