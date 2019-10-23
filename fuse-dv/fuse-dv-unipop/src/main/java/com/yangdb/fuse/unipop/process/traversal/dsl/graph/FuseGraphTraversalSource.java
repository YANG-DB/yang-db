package com.yangdb.fuse.unipop.process.traversal.dsl.graph;

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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.UnionStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Arrays;

/**
 * Created by Roman on 1/29/2018.
 */
public class FuseGraphTraversalSource extends GraphTraversalSource {
    //region Constructors
    public FuseGraphTraversalSource(Graph graph, TraversalStrategies traversalStrategies) {
        super(graph, traversalStrategies);
    }

    public FuseGraphTraversalSource(Graph graph) {
        super(graph);
    }
    //endregion

    //region GraphTraversalSource Implementation
    public GraphTraversal<Vertex, Vertex> V(Object... vertexIds) {
        FuseGraphTraversalSource clone = (FuseGraphTraversalSource)this.clone();
        clone.bytecode.addStep("V", vertexIds);
        GraphTraversal.Admin<Vertex, Vertex> traversal = new FuseGraphTraversal<>(clone);
        return traversal.addStep(new GraphStep<>(traversal, Vertex.class, true, vertexIds));
    }

    public GraphTraversal<Edge, Edge> E(Object... edgesIds) {
        FuseGraphTraversalSource clone = (FuseGraphTraversalSource)this.clone();
        clone.bytecode.addStep("E", edgesIds);
        GraphTraversal.Admin<Edge, Edge> traversal = new FuseGraphTraversal<>(clone);
        return traversal.addStep(new GraphStep<>(traversal, Edge.class, true, edgesIds));
    }

    public <S, E2> GraphTraversal<S, E2> union(final Traversal<?, E2>... unionTraversals) {
        FuseGraphTraversalSource clone = (FuseGraphTraversalSource)this.clone();
        clone.bytecode.addStep(GraphTraversal.Symbols.union, unionTraversals);
        GraphTraversal.Admin<Edge, Edge> traversal = new FuseGraphTraversal<>(clone);
        return traversal.addStep(new UnionStep(traversal, Arrays.copyOf(unionTraversals, unionTraversals.length, Traversal.Admin[].class)));
    }
    //endregion
}
