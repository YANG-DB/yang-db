package com.kayhut.fuse.assembly;

import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class KnowledgeRoutedSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        if (!ontology.getOnt().equals("Knowledge")) {
            return null;
        }

        return new KnowledgeSchemaProvider();
    }
    //endregion

    //region KnowledgeSchemaProvider
    public static class KnowledgeSchemaProvider implements GraphElementSchemaProvider {
        //region GraphElementSchemaProvider Implementation
        @Override
        public Optional<GraphVertexSchema> getVertexSchema(String label) {
            switch (label) {
                case "Entity":
                    return Optional.of(new GraphVertexSchema.Impl(
                            "Entity",
                            "Entity",
                            Optional.of(new GraphElementRouting.Impl(
                                    new GraphElementPropertySchema.Impl("logicalId", "string")
                            )),
                            Optional.of(new IndexPartitions.Impl(
                                    "logicalId",
                                    new IndexPartitions.Partition.Range.Impl<>("e000", "e010", "entity1"),
                                    new IndexPartitions.Partition.Range.Impl<>("e010", "e020", "entity2")
                            )),
                            Collections.emptyList()
                    ));

                case "Value":
                    return Optional.of(new GraphVertexSchema.Impl(
                            "Value",
                            "Value",
                            Optional.empty(),
                            Optional.of(new IndexPartitions.Impl(
                                    "logicalId",
                                    new IndexPartitions.Partition.Range.Impl<>("e000", "e010", "entity1"),
                                    new IndexPartitions.Partition.Range.Impl<>("e010", "e020", "entity2")
                            )),
                            Collections.emptyList()
                    ));

                default: return Optional.empty();
            }
        }

        @Override
        public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
            switch (label) {
                case "hasValue":
                    return Optional.of(new GraphEdgeSchema.Impl(
                            "hasValue",
                            "Value",
                            Optional.of(new GraphEdgeSchema.End.Impl(
                                    "entityId",
                                    Optional.of("Entity"),
                                    Collections.emptyList(),
                                    Optional.of(new GraphElementRouting.Impl(
                                            new GraphElementPropertySchema.Impl("logicalId", "string")
                                    )),
                                    Optional.of(new IndexPartitions.Impl("logicalId",
                                            new IndexPartitions.Partition.Range.Impl<>("e000", "e010", "entity1"),
                                            new IndexPartitions.Partition.Range.Impl<>("e010", "e020", "entity2"))))),
                            Optional.of(new GraphEdgeSchema.End.Impl(
                                    "_id",
                                    Optional.of("Value"),
                                    Arrays.asList(
                                            new GraphRedundantPropertySchema.Impl("context", "context", "string"),
                                            new GraphRedundantPropertySchema.Impl("security1", "security1", "string"),
                                            new GraphRedundantPropertySchema.Impl("security2", "security2", "string"),
                                            new GraphRedundantPropertySchema.Impl("propertyId", "propertyId", "string"),
                                            new GraphRedundantPropertySchema.Impl("stringValue", "stringValue", "string"),
                                            new GraphRedundantPropertySchema.Impl("textValue", "textValue", "string"),
                                            new GraphRedundantPropertySchema.Impl("intValue", "intValue", "int"),
                                            new GraphRedundantPropertySchema.Impl("dateValue", "dateValue", "date")),
                                    Optional.of(new GraphElementRouting.Impl(
                                            new GraphElementPropertySchema.Impl("logicalId", "string")
                                    )),
                                    Optional.of(new IndexPartitions.Impl("logicalId",
                                            new IndexPartitions.Partition.Range.Impl<>("e000", "e010", "entity1"),
                                            new IndexPartitions.Partition.Range.Impl<>("e010", "e020", "entity2"))))),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Collections.emptyList()
                    ));

                default: return Optional.empty();
            }
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
            Optional<GraphEdgeSchema> graphEdgeSchema = getEdgeSchema(label);
            if (graphEdgeSchema.isPresent()) {
                return Collections.singletonList(graphEdgeSchema.get());
            }

            return Collections.emptyList();
        }

        @Override
        public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
            return Optional.empty();
        }

        @Override
        public Iterable<String> getVertexLabels() {
            return Arrays.asList("Entity", "Value");
        }

        @Override
        public Iterable<String> getEdgeLabels() {
            return Collections.singletonList("hasValue");
        }
        //endregion
    }
    //endregion
}
