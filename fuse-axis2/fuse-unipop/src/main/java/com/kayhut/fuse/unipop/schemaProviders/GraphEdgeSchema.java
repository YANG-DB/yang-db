package com.kayhut.fuse.unipop.schemaProviders;

import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.Optional;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphEdgeSchema extends GraphElementSchema {
    default Class getSchemaElementType() {
        return Edge.class;
    }

    interface End {
        String getIdField();
        Optional<String> getLabel();
        Optional<GraphRedundantPropertySchema> getRedundantProperty(GraphElementPropertySchema property);
    }

    interface Direction {
        String getField();
        Object getInValue();
        Object getOutValue();
    }

    Optional<End> getSource();
    Optional<End> getDestination();

    Optional<Direction> getDirection();

}
