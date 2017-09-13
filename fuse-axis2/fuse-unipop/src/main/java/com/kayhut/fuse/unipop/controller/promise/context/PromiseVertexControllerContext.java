package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Optional;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseVertexControllerContext implements VertexControllerContext {
    //region Constructors
    public PromiseVertexControllerContext(List<Vertex> vertices,
                                          Optional<TraversalConstraint> constraint,
                                          GraphElementSchemaProvider schemaProvider) {
        this.bulkVertices = vertices;
        this.constraint = constraint;
        this.schema = schemaProvider;
    }
    //endregion

    //region VertexControllerContext Implementation
    @Override
    public List<Vertex> getBulkVertices() {
        return bulkVertices;
    }

    @Override
    public Optional<TraversalConstraint> getConstraint() {
        return constraint;
    }

    @Override
    public ElementType getElementType() {
        return ElementType.edge;
    }

    @Override
    public GraphElementSchemaProvider getSchemaProvider() {
        return this.schema;
    }

    @Override
    public Direction getDirection() {
        return Direction.OUT;
    }
    //endregion

    //region Fields
    private List<Vertex> bulkVertices;
    private Optional<TraversalConstraint> constraint;
    private GraphElementSchemaProvider schema;
    //endregion
}
