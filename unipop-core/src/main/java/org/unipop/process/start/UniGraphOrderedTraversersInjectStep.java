package org.unipop.process.start;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.*;

public class UniGraphOrderedTraversersInjectStep<S> extends AbstractStep<S, S> {
    //region Constructors
    public UniGraphOrderedTraversersInjectStep(Traversal.Admin traversal, List<Traverser.Admin<S>> starts) {
        super(traversal);

        this.starts = new ArrayDeque<>(starts);
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<S> processNextStart() throws NoSuchElementException {
        if (this.starts.isEmpty()) {
            throw FastNoSuchElementException.instance();
        }

        return this.starts.remove();
    }

    @Override
    public void addStarts(final Iterator<org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin<S>> starts) {
        this.starts.addAll(Stream.ofAll(() -> starts).toJavaList());
    }

    @Override
    public void addStart(final org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin<S> start) {
        this.starts.add(start);
    }

    @Override
    public void reset() {
        super.reset();
        this.starts.clear();
    }
    //endregion

    //region Fields
    private Queue<Traverser.Admin<S>> starts;
    //endregion
}
