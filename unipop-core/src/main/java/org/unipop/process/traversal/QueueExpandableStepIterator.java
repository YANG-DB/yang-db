package org.unipop.process.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ExpandableStepIterator;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.TraverserSet;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.*;

/*public class QueueExpandableStepIterator<S> extends ExpandableStepIterator<S> {
    //region Constructors
    public QueueExpandableStepIterator(Step<S, ?> hostStep) {
        super(hostStep);
        this.traversers = new ArrayDeque<>();
    }
    //endregion

    //region ExpandableStepIterator Implementation
    @Override
    public boolean hasNext() {
        return !this.traversers.isEmpty() || this.hostStep.getPreviousStep().hasNext();
    }

    @Override
    public Traverser.Admin<S> next() {
        if (!this.traversers.isEmpty()) {
            return this.traversers.remove();
        } else {
            if (this.hostStep.getPreviousStep().hasNext()) {
                return this.hostStep.getPreviousStep().next();
            }

            if (!this.traversers.isEmpty()) {
                return this.traversers.remove();
            }

            throw FastNoSuchElementException.instance();
        }
    }

    @Override
    public void add(final Iterator<Traverser.Admin<S>> iterator) {
        iterator.forEachRemaining(traverser -> this.traversers.add(traverser));
    }

    @Override
    public void add(final Traverser.Admin<S> traverser) {
        this.traversers.add(traverser);
    }

    @Override
    public String toString() {
        return this.traversers.toString();
    }

    @Override
    public void clear() {
        this.traversers.clear();
    }
    //endregion

    //region Fields
    private Queue<Traverser.Admin<S>> traversers;
    //endregion
}*/
