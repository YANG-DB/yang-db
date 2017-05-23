package com.kayhut.fuse.unipop.schemaProviders;

import java.util.Optional;

/**
 * Created by moti on 5/17/2017.
 */
public interface GraphLayoutProvider {
    class NoneRedundant implements GraphLayoutProvider {
        public static GraphLayoutProvider getInstance() {
            return instance;
        }
        private static GraphLayoutProvider instance = new NoneRedundant();

        //region GraphLayoutProvider Implementation
        @Override
        public Optional<GraphRedundantPropertySchema> getRedundantVertexProperty(String edgeType, String property) {
            return Optional.empty();
        }

        @Override
        public Optional<GraphRedundantPropertySchema> getRedundantVertexPropertyByPushdownName(String edgeType, String property) {
            return Optional.empty();
        }
        //endregion
    }

    Optional<GraphRedundantPropertySchema> getRedundantVertexProperty(String edgeType, String property);

    Optional<GraphRedundantPropertySchema> getRedundantVertexPropertyByPushdownName(String edgeType, String property);

}
