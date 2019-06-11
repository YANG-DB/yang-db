package org.unipop.process.union;

/*-
 * #%L
 * UniGraphUnionStep.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.util.Attachable;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.EmptyIterator;
import org.unipop.process.UniBulkStep;
import org.unipop.process.traverser.UniGraphTraverserStep;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sbarzilay on 6/6/16.
 */
public class UniGraphUnionStep<S,E> extends UniBulkStep<S,E> implements TraversalParent{
    Iterator<Traverser.Admin<E>> results = EmptyIterator.instance();
    List<Traversal.Admin<?, E>> unionTraversals;

    public UniGraphUnionStep(Traversal.Admin traversal, UniGraph graph, final Traversal.Admin<?, E>... unionTraversals) {
        super(traversal, graph);
        this.unionTraversals = Arrays.asList(unionTraversals);
        this.unionTraversals.forEach(t -> t.addStep(new UniGraphTraverserStep<>(t)));
    }

    @Override
    public  List<Traversal.Admin<S, E>> getGlobalChildren() {
        return unionTraversals.stream().map(t -> ((Traversal.Admin<S,E>) t)).collect(Collectors.toList());
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.unionTraversals.stream().map(Traversal.Admin::getTraverserRequirements).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    protected Iterator<Traverser.Admin<E>> process(List<Traverser.Admin<S>> traversers) {
        List<Traverser.Admin<S>> bulkedTraversers = traversers.stream().collect(Collectors.groupingBy(Attachable::get)).entrySet().stream().map(entry -> {
            Traverser.Admin<S> sAdmin = entry.getValue().get(0);
            sAdmin.setBulk(entry.getValue().size());
            return sAdmin;
        }).collect(Collectors.toList());

        List<Traverser.Admin<E>> results = new ArrayList<>();
        this.unionTraversals.forEach(t->{
            bulkedTraversers.forEach(((Traversal.Admin<S, E>) t)::addStart);
            while(t.hasNext())
                results.add((Traverser.Admin<E>) t.next());
        });
        results.forEach(t -> t.setBulk(1));
        return results.iterator();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.unionTraversals);
    }
}
