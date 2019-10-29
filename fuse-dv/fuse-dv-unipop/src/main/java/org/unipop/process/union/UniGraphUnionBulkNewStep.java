package org.unipop.process.union;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
 * #L%
 */

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.unipop.process.UniBulkStep;
import org.unipop.process.start.UniGraphOrderedTraversersInjectStep;
import org.unipop.structure.UniGraph;

import java.util.*;

public class UniGraphUnionBulkNewStep<S, E> extends UniBulkStep<S,E> implements TraversalParent {

    //region Constructors
    public UniGraphUnionBulkNewStep(Traversal.Admin traversal, UniGraph graph, List<Traversal.Admin<S,E>> unionTraversals) {
        super(traversal,graph);
        this.unionTraversals = new ArrayList<>();
        //add traversals to global
        Stream.ofAll(unionTraversals).forEach(this::addGlobalChild);
    }

    public UniGraphUnionBulkNewStep(Traversal.Admin traversal,UniGraph graph) {
        super(traversal,graph);
        this.unionTraversals = new ArrayList<>();
    }
    //endregion

    //region Step Implementation
    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.PATH);
    }

    @Override
    public List<Traversal.Admin<S, E>> getGlobalChildren() {
        return this.unionTraversals;
    }

    @Override
    public void addGlobalChild(Traversal.Admin<?, ?> globalChildTraversal) {
        //add traversal & enrich with integrated start step
        this.unionTraversals.add(this.integrateChild(globalChildTraversal));

    }
    //endregion

    //region UniBulkStep Implementation
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

    @Override
    protected java.util.Iterator<Traverser.Admin<E>> process(List<Traverser.Admin<S>> traversers) {
        Stream.ofAll(this.unionTraversals).forEach(Traversal.Admin::reset);
        Stream.ofAll(this.unionTraversals).forEach(tr->tr.addStarts(traversers.iterator()));
        return new UnionIterator<>(this.unionTraversals);
    }

    @Override
    public void reset() {
        super.reset();
        this.traversal.reset();
        Stream.ofAll(this.unionTraversals).forEach(Traversal.Admin::reset);
    }

        //endregion

    //region Object Implementation
    @Override
    public UniGraphUnionBulkNewStep<S, E> clone() {
        UniGraphUnionBulkNewStep<S, E> clone = (UniGraphUnionBulkNewStep<S, E>) super.clone();
        clone.unionTraversals = new ArrayList<>();
        unionTraversals.stream().map(t->clone.unionTraversals.add(t.clone()));
        return clone;
    }
    //endregion

    private static class UnionIterator<S, E> implements java.util.Iterator<Traverser.Admin<E>> {
        private final List<Traversal.Admin<S, E>> unionTraversals;
        private int currentTraversalInWork ;

        //region Constructors
        public UnionIterator(List<Traversal.Admin<S, E>> unionTraversals) {
            this.unionTraversals = unionTraversals;
        }
        //endregion

        //region Iterator Implementation
        @Override
        public boolean hasNext() {
            if(unionTraversals==null)
                return false;
            if(currentTraversalInWork >= unionTraversals.size())
                return false;

            boolean hasNext = unionTraversals.get(currentTraversalInWork).hasNext();
            //try to get the next results (hasNext will drain the next items from the traversal)
            while(!hasNext && (++currentTraversalInWork<unionTraversals.size())) {
                //iterate over the union branched until result is found or all traversals tested
                hasNext |= unionTraversals.get(currentTraversalInWork).hasNext();
            }
            return hasNext;
        }

        @Override
        public Traverser.Admin<E> next() {
            if(unionTraversals==null)
                throw FastNoSuchElementException.instance();

            Traversal.Admin<?,E> traversalInWork = null;
            if(currentTraversalInWork >= unionTraversals.size())
                throw FastNoSuchElementException.instance();
            //get current traversal being iterated
            traversalInWork = unionTraversals.get(currentTraversalInWork);

            try {
                return traversalInWork.nextTraverser();
            } catch (FastNoSuchElementException e) {
                //if traversal has no more results - go to next traversal
                Traverser.Admin<E> result = null;
                while (result==null) {
                    if (++currentTraversalInWork >= unionTraversals.size())
                        throw FastNoSuchElementException.instance();

                    try {
                        traversalInWork = unionTraversals.get(currentTraversalInWork);
                        result = traversalInWork.nextTraverser();
                    } catch (NoSuchElementException e1) {}
                }
                return result;
            }

        }
        //endregion

        //region Fields
        //endregion
    }

    //region Fields
    private List<Traversal.Admin<S,E>> unionTraversals;
    //endregion
}
