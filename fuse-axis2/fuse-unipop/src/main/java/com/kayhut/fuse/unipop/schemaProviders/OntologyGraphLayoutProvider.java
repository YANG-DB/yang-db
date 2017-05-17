package com.kayhut.fuse.unipop.schemaProviders;

import java.util.Optional;

/**
 * Created by moti on 5/17/2017.
 */
public interface OntologyGraphLayoutProvider {
    Optional<GraphRedundantPropertySchema> getRedundantVertexProperty(String edgeType, String property);

    Optional<GraphRedundantPropertySchema> getRedundantVertexPropertyByPushdownName(String edgeType, String property);

}
