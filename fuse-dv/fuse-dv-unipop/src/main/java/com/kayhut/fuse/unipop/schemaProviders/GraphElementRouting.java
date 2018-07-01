package com.kayhut.fuse.unipop.schemaProviders;

/**
 * Created by r on 3/19/2015.
 */
public interface GraphElementRouting {
    GraphElementPropertySchema getRoutingProperty();

    class Impl implements GraphElementRouting {
        //region Constructors
        public Impl(GraphElementPropertySchema routingProperty) {
            this.routingProperty = routingProperty;
        }
        //endregion

        //region GraphElementRouting Implementation
        @Override
        public GraphElementPropertySchema getRoutingProperty() {
            return this.routingProperty;
        }
        //endregion

        //region Fields
        private GraphElementPropertySchema routingProperty;
        //endregion
    }
}
