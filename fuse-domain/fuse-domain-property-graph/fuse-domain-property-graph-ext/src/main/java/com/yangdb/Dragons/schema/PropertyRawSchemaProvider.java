package com.yangdb.Dragons.schema;

import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.List;

public class PropertyRawSchemaProvider implements RawSchema {

    @Inject
    public PropertyRawSchemaProvider() {

    }

    @Override
    public IndexPartitions getPartition(String type) {
        return null;
    }

    @Override
    public String getIdFormat(String type) {
        return null;
    }

    @Override
    public String getPrefix(String type) {
        return null;
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return null;
    }

    @Override
    public Iterable<String> indices() {
        return null;
    }
}
