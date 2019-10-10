package org.unipop.process.optional;

/*-
 *
 * UniGraphOptionalStep.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

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
public class UniGraphOptionalStep<S, E> extends UniBulkStep<S, E> implements TraversalParent {
    //region Constructors
    public UniGraphOptionalStep(Traversal.Admin traversal, UniGraph graph) {
        super(traversal, graph);
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
    protected java.util.Iterator<Traverser.Admin<E>> process(List<Traverser.Admin<S>> traversers) {
        Stream.ofAll(traversers)
                .forEach(traverser -> traverser.<Map<String, Object>>sack().put(this.getId(), traverser));

        this.optionalTraversal.reset();
        this.optionalTraversal.addStarts(Stream.ofAll(traversers).map(Traverser.Admin::split).iterator());

        return new OptionalIterator<>(traversers, this.optionalTraversal.getEndStep(), this.getId());
    }

    @Override
    public void reset() {
        super.reset();
        this.optionalTraversal.reset();
    }
    //endregion

    //region TraversalParent Implementation
    @Override
    public List<Traversal.Admin<S, E>> getGlobalChildren() {
        return Collections.singletonList(this.optionalTraversal);
    }

    @Override
    public void addGlobalChild(Traversal.Admin<?, ?> globalChildTraversal) {
        if (this.optionalTraversal != null) {
            throw new IllegalStateException("Only one global child traversal is allowed: " + this.getClass().getCanonicalName());
        }

        this.optionalTraversal = this.integrateChild(globalChildTraversal);
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
    public UniGraphOptionalStep<S, E> clone() {
        UniGraphOptionalStep<S, E> clone = (UniGraphOptionalStep<S, E>) super.clone();
        clone.optionalTraversal = null;

        if (this.optionalTraversal != null) {
            clone.addGlobalChild(this.optionalTraversal.clone());
        }

        return clone;
    }
    //endregion

    //region Fields
    private Traversal.Admin<S, E> optionalTraversal;
    //endregion

    //region OptionalIterator
    private static class OptionalIterator<S, E> implements java.util.Iterator<Traverser.Admin<E>> {
        //region Constructors
        public OptionalIterator(List<Traverser.Admin<S>> starts, java.util.Iterator<Traverser.Admin<E>> optionalIterator, String optionalKey) {
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
        private java.util.Iterator<Traverser.Admin<E>> current;
        private boolean positiveOptionalsPhase;
        //endregion
    }
    //endregion
}
