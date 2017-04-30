package com.kayhut.fuse.unipop.controller.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class PromiseVertexFilterControllerContext implements EdgeConstraintContext{

    private List<Vertex> startVertices;
    private Optional<TraversalConstraint> edgeConstraint;
    private GraphElementSchemaProvider schema;

    public PromiseVertexFilterControllerContext(List<Vertex> vertices,
                                                Optional<TraversalConstraint> constraint,
                                                GraphElementSchemaProvider schemaProvider) {
        startVertices = vertices;
        edgeConstraint = constraint;
        schema = schemaProvider;
    }

    public List<Vertex> getStartVertices() {
        return startVertices;
    }

    public Optional<TraversalConstraint> getEdgeConstraint() {
        return edgeConstraint;
    }

    public GraphElementSchemaProvider getSchema() {
        return schema;
    }
}
