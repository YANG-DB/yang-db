package org.unipop.util.iterator;

import java.util.Collections;
import java.util.Iterator;

public class ConcatIterator<T> implements Iterator<T> {

    private final Iterator<? extends Iterator<? extends T>> iterators;
    private Iterator<? extends T> current;

    public ConcatIterator(Iterator<? extends Iterator<? extends T>> iterators) {
        this.current = Collections.emptyIterator();
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        while (!current.hasNext() && iterators.hasNext()) {
            current = iterators.next();
        }
        return current.hasNext();
    }

    @Override
    public T next() {
        return current.next();
    }
}
