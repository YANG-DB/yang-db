package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;

/**
 * Created by r on 1/16/2015.
 */
public interface GraphVertexSchema extends GraphElementSchema {
    default Class getSchemaElementType() {
        return Vertex.class;
    }

    class Impl extends GraphElementSchema.Impl implements GraphVertexSchema {
        //region Constructors
        public Impl(String label, GraphElementRouting routing) {
            super(label, routing);
        }

        public Impl(String label, IndexPartitions indexPartitions) {
            super(label, indexPartitions);
        }

        public Impl(String label, Optional<GraphElementRouting> routing, Optional<IndexPartitions> indexPartitions) {
            super(label, routing, indexPartitions);
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            super(label, constraint, routing, indexPartitions, properties);
        }
        //endregion
    }
}
