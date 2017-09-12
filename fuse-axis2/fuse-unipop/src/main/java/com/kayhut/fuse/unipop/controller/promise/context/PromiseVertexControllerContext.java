package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseVertexControllerContext implements ConstraintContext {

    private List<Vertex> startVertices;
    private Optional<TraversalConstraint> edgeConstraint;
    private GraphElementSchemaProvider schema;

    public PromiseVertexControllerContext(List<Vertex> vertices,
                                          Optional<TraversalConstraint> constraint,
                                          GraphElementSchemaProvider schemaProvider) {
        startVertices = vertices;
        edgeConstraint = constraint;
        schema = schemaProvider;
    }

    public List<Vertex> getStartVertices() {
        return startVertices;
    }

    public Optional<TraversalConstraint> getConstraint() {
        return edgeConstraint;
    }

    @Override
    public ElementType getElementType() {
        return ElementType.edge;
    }

    @Override
    public GraphElementSchemaProvider getSchemaProvider() {
        return this.schema;
    }
}
