package com.yangdb.fuse.executor.utils;

import com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema.getIndexPartitions;

public abstract class MockSchemaUtils {

    public static RawSchema createSchema(GraphElementSchemaProvider schemaProvider) {
        return new RawSchema() {
            @Override
            public IndexPartitions getPartition(Ontology ontology, String type) {
                return getIndexPartitions(schemaProvider,type);
            }

            @Override
            public IndexPartitions getPartition(String type) {
                return getIndexPartitions(schemaProvider,type);
            }

            @Override
            public String getIdPrefix(String type) {
                return "";
            }

            @Override
            public String getIdFormat(String type) {
                return "";
            }

            @Override
            public String getIndexPrefix(String type) {
                return "";
            }

            @Override
            public List<IndexPartitions.Partition> getPartitions(String type) {
                return StreamSupport.stream(getPartition(type).getPartitions().spliterator(), false)
                        .collect(Collectors.toList());

            }

            @Override
            public Iterable<String> indices() {
                return IndexProviderRawSchema.indices(schemaProvider);
            }
        };
    }
}
