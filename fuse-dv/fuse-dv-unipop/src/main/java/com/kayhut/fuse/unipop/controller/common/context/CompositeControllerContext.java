package com.kayhut.fuse.unipop.controller.common.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by roman.margolis on 15/11/2017.
 */
public interface CompositeControllerContext extends VertexControllerContext{
    Optional<ElementControllerContext> getElementControllerContext();
    Optional<VertexControllerContext> getVertexControllerContext();

    class Impl implements CompositeControllerContext {
        //region Constructors
        public Impl(
                ElementControllerContext elementControllerContext,
                VertexControllerContext vertexControllerContext) {
            this.elementControllerContext = Optional.ofNullable(elementControllerContext);
            this.vertexControllerContext = Optional.ofNullable(vertexControllerContext);
        }
        //endregion

        //region CompositeControllerContext
        @Override
        public Optional<ElementControllerContext> getElementControllerContext() {
            return this.elementControllerContext;
        }

        @Override
        public Optional<VertexControllerContext> getVertexControllerContext() {
            return this.vertexControllerContext;
        }
        //endregion

        //region VertexControllerContext Implementation
        @Override
        public int getLimit() {
            return this.elementControllerContext.map(LimitContext::getLimit)
                    .orElseGet(() -> this.vertexControllerContext.get().getLimit());
        }

        @Override
        public UniGraph getGraph() {
            return this.elementControllerContext.map(GraphContext::getGraph)
                    .orElseGet(() -> this.vertexControllerContext.get().getGraph());
        }

        @Override
        public ElementType getElementType() {
            return this.elementControllerContext.map(ElementContext::getElementType)
                    .orElseGet(() -> this.vertexControllerContext.get().getElementType());
        }

        @Override
        public Direction getDirection() {
            return this.vertexControllerContext
                    .map(DirectionContext::getDirection)
                    .orElse(Direction.OUT);

        }

        @Override
        public Iterable<HasContainer> getSelectPHasContainers() {
            return this.elementControllerContext
                    .map(SelectContext::getSelectPHasContainers)
                    .orElseGet(() -> this.vertexControllerContext.get().getSelectPHasContainers());
        }

        @Override
        public Iterable<Vertex> getBulkVertices() {
            return this.vertexControllerContext
                    .map(BulkContext::getBulkVertices)
                    .orElseGet(Collections::emptyList);

        }

        @Override
        public Vertex getVertex(Object id) {
            return vertexControllerContext
                    .map(vertexControllerContext -> vertexControllerContext.getVertex(id))
                    .orElse(null);
        }

        @Override
        public GraphElementSchemaProvider getSchemaProvider() {
            return elementControllerContext
                    .map(SchemaProviderContext::getSchemaProvider)
                    .orElseGet(() -> this.vertexControllerContext.get().getSchemaProvider());
        }

        @Override
        public Optional<TraversalConstraint> getConstraint() {
            return elementControllerContext
                    .map(ConstraintContext::getConstraint)
                    .orElseGet(() -> this.vertexControllerContext.get().getConstraint());
        }
        //endegion

        //region Fields
        private Optional<ElementControllerContext> elementControllerContext;
        private Optional<VertexControllerContext> vertexControllerContext;
        //endregion
    }
}
