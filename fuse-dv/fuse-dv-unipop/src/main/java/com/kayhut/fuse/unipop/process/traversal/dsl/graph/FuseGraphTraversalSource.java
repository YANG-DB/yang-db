package com.kayhut.fuse.unipop.process.traversal.dsl.graph;

import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

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
    //endregion
}