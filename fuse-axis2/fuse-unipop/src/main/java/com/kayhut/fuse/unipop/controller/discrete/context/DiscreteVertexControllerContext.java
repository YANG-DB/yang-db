package com.kayhut.fuse.unipop.controller.discrete.context;

import com.kayhut.fuse.unipop.controller.common.context.BulkContext;
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
 * Created by roman.margolis on 13/09/2017.
 */
public class DiscreteVertexControllerContext implements VertexControllerContext {
    //region Constructors
    public DiscreteVertexControllerContext(
            List<Vertex> bulkVertices,
            Direction direction,
            Optional<TraversalConstraint> constraint,
            ElementType elementType,
            GraphElementSchemaProvider schemaProvider) {
        this.constraint = constraint;
        this.elementType = elementType;
        this.schemaProvider = schemaProvider;
        this.bulkVertices = bulkVertices;
        this.direction = direction;
    }
    //endregion

    //region Properties
    @Override
    public ElementType getElementType() {
        return this.elementType;
    }

    @Override
    public GraphElementSchemaProvider getSchemaProvider() {
        return this.schemaProvider;
    }

    @Override
    public Optional<TraversalConstraint> getConstraint() {
        return constraint;
    }

    @Override
    public List<Vertex> getBulkVertices() {
        return this.bulkVertices;
    }

    @Override
    public Direction getDirection() {
        return null;
    }
    //endregion

    //region Fields
    private Optional<TraversalConstraint> constraint;
    private ElementType elementType;
    private GraphElementSchemaProvider schemaProvider;
    private List<Vertex> bulkVertices;
    private Direction direction;
    //endregion
}
