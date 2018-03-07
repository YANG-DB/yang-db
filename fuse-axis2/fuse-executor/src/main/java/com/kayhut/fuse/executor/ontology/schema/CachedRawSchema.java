package com.kayhut.fuse.executor.ontology.schema;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by roman.margolis on 01/03/2018.
 */
public class CachedRawSchema implements RawSchema {
    public static final String rawSchemaParameter = "CachedRawSchema.@rawSchema";

    //region Constructors
    @Inject
    public CachedRawSchema(
            @Named(rawSchemaParameter) RawSchema rawSchema) {
        this.rawSchema = rawSchema;

        this.indexPartitions = Collections.synchronizedMap(new HashMap<>());
        this.indexPartitionsPartitions = Collections.synchronizedMap(new HashMap<>());
        this.idFormats = Collections.synchronizedMap(new HashMap<>());
    }
    //endregion

    //region RawSchema Implementation
    @Override
    public IndexPartitions getPartition(String type) {
        return this.indexPartitions.computeIfAbsent(type, t -> this.rawSchema.getPartition(t));
    }

    @Override
    public String getIdFormat(String type) {
        return this.idFormats.computeIfAbsent(type, t -> this.rawSchema.getIdFormat(t));
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return this.indexPartitionsPartitions.computeIfAbsent(type, t -> this.rawSchema.getPartitions(t));
    }

    @Override
    public Iterable<String> indices() {
        if (this.indices == null) {
            this.indices = this.rawSchema.indices();
        }

        return this.indices;
    }
    //endregion

    //region Fields
    private RawSchema rawSchema;

    private Map<String, IndexPartitions> indexPartitions;
    private Map<String, List<IndexPartitions.Partition>> indexPartitionsPartitions;
    private Map<String, String> idFormats;

    private Iterable<String> indices;
    //endregion
}
