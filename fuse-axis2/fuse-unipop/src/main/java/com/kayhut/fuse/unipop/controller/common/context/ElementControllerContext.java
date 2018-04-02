package com.kayhut.fuse.unipop.controller.common.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface ElementControllerContext extends ConstraintContext, SchemaProviderContext, ElementContext, GraphContext, LimitContext, SelectContext {
    class Impl implements ElementControllerContext {
        //region Constructors
        public Impl(
                UniGraph graph,
                ElementType elementType,
                GraphElementSchemaProvider schemaProvider,
                Optional<TraversalConstraint> constraint,
                Iterable<HasContainer> selectPHasContainers,
                int limit) {
            this.graph = graph;
            this.elementType = elementType;
            this.schemaProvider = schemaProvider;
            this.constraint = constraint;
            this.selectPHasContainers = selectPHasContainers;
            this.limit = limit;
        }
        //endregion

        //region ElementControllerContext Implementation
        @Override
        public UniGraph getGraph() {
            return graph;
        }

        @Override
        public ElementType getElementType() {
            return elementType;
        }

        @Override
        public GraphElementSchemaProvider getSchemaProvider() {
            return schemaProvider;
        }

        @Override
        public Optional<TraversalConstraint> getConstraint() {
            return constraint;
        }

        @Override
        public int getLimit() {
            return limit;
        }

        @Override
        public Iterable<HasContainer> getSelectPHasContainers() {
            return selectPHasContainers;
        }
        //endregion

        //region Fields
        private UniGraph graph;
        private ElementType elementType;
        private GraphElementSchemaProvider schemaProvider;
        private Optional<TraversalConstraint> constraint;
        private int limit;
        private Iterable<HasContainer> selectPHasContainers;
        //endregion
    }
}
