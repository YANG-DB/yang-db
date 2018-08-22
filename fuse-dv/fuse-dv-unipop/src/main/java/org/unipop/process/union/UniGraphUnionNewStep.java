package org.unipop.process.union;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.unipop.process.UniBulkStep;
import org.unipop.process.start.UniGraphOrderedTraversersInjectStep;
import org.unipop.structure.UniGraph;

import java.util.*;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class UniGraphUnionNewStep<S, E> extends UniBulkStep<S, E> implements TraversalParent {
    private final List<Traversal.Admin> unionTraversals;

    //region Constructors
    public UniGraphUnionNewStep(Traversal.Admin traversal, UniGraph graph, List<Traversal.Admin> unionTraversals) {
        super(traversal, graph);
        this.unionTraversals = unionTraversals;
    }
    //endregion

    //region Step Implementation
    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.PATH);
    }
    //endregion

    //region UniBulkStep Implementation
    @Override
    protected Iterator<Traverser.Admin<E>> process(List<Traverser.Admin<S>> traversers) {
        Stream.ofAll(traversers)
                .forEach(traverser -> traverser.<Map<String, Object>>sack().put(this.getId(), traverser));

        this.branches.reset();
        this.branches.addStarts(Stream.ofAll(traversers).map(Traverser.Admin::split).iterator());

        return new OptionalIterator<>(traversers, this.branches.getEndStep(), this.getId());
    }

    @Override
    public void reset() {
        super.reset();
        this.branches.reset();
    }
    //endregion

    //region TraversalParent Implementation
    @Override
    public List<Traversal.Admin<S, E>> getGlobalChildren() {
        return Collections.singletonList(this.branches);
    }

    @Override
    public void addGlobalChild(Traversal.Admin<?, ?> globalChildTraversal) {
        if (this.branches != null) {
            throw new IllegalStateException("Only one global child traversal is allowed: " + this.getClass().getCanonicalName());
        }

        this.branches = this.integrateChild(globalChildTraversal);
    }

    @Override
    public <S, E> Traversal.Admin<S, E> integrateChild(final Traversal.Admin<?, ?> childTraversal) {
        if (null == childTraversal) {
            return null;
        } else {
            childTraversal.setParent(this);
            childTraversal.getSideEffects().mergeInto(this.asStep().getTraversal().getSideEffects());
            childTraversal.setSideEffects(this.asStep().getTraversal().getSideEffects());

            childTraversal.addStep(0, new UniGraphOrderedTraversersInjectStep<>(childTraversal, Collections.emptyList()));

            return (Traversal.Admin<S, E>)childTraversal;
        }
    }
    //endregion

    //region Object Implementation
    @Override
    public UniGraphUnionNewStep<S, E> clone() {
        UniGraphUnionNewStep<S, E> clone = (UniGraphUnionNewStep<S, E>) super.clone();
        clone.branches = null;

        if (this.branches != null) {
            clone.addGlobalChild(this.branches.clone());
        }

        return clone;
    }
    //endregion

    //region Fields
    private Traversal.Admin<S, E> branches;
    //endregion

    //region OptionalIterator
    private static class OptionalIterator<S, E> implements Iterator<Traverser.Admin<E>> {
        //region Constructors
        public OptionalIterator(List<Traverser.Admin<S>> starts, Iterator<Traverser.Admin<E>> optionalIterator, String optionalKey) {
            this.foundStarts = new HashSet<>();

            this.starts = starts;
            this.current =
                    Stream.ofAll(() -> optionalIterator)
                    .map(end -> {
                        Map<String, Traverser.Admin<S>> sack = end.sack();
                        this.foundStarts.add(sack.remove(optionalKey));
                        if (sack.isEmpty()) {
                            sack.clear();
                        }
                        return end;
                    }).iterator();

            this.positiveOptionalsPhase = true;
        }
        //endregion

        //region Iterator Implementation
        @Override
        public boolean hasNext() {
            boolean currentHasNext = this.current.hasNext();
            if (!currentHasNext && this.positiveOptionalsPhase) {
                this.positiveOptionalsPhase = false;
                this.current = Stream.ofAll(starts)
                        .filter(start -> !this.foundStarts.contains(start))
                        .map(start -> (Traverser.Admin<E>)start)
                        .iterator();

                currentHasNext = this.current.hasNext();
            }

            return currentHasNext;
        }

        @Override
        public Traverser.Admin<E> next() {
            return this.current.next();
        }
        //endregion

        //region Fields
        private List<Traverser.Admin<S>> starts;
        private Set<Traverser.Admin<S>> foundStarts;
        private Iterator<Traverser.Admin<E>> current;
        private boolean positiveOptionalsPhase;
        //endregion
    }
    //endregion
}
