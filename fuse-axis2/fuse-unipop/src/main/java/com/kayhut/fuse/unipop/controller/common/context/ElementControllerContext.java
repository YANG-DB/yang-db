package com.kayhut.fuse.unipop.controller.common.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface ElementControllerContext extends ConstraintContext, SchemaProviderContext, ElementContext, GraphContext {
    class Default implements ElementControllerContext {
        //region Constructors
        public Default(
                UniGraph graph,
                ElementType elementType,
                GraphElementSchemaProvider schemaProvider,
                Optional<TraversalConstraint> constraint) {
            this.graph = graph;
            this.elementType = elementType;
            this.schemaProvider = schemaProvider;
            this.constraint = constraint;
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
        //endregion

        //region Fields
        private UniGraph graph;
        private ElementType elementType;
        private GraphElementSchemaProvider schemaProvider;
        private Optional<TraversalConstraint> constraint;
        //endregion
    }
}
