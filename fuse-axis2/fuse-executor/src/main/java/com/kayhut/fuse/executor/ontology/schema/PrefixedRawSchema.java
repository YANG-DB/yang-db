package com.kayhut.fuse.executor.ontology.schema;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions.Partition.Range;
import javaslang.collection.Stream;

import java.util.Collection;
import java.util.List;

/**
 * Created by roman.margolis on 01/03/2018.
 */
public class PrefixedRawSchema implements RawSchema {
    //region Static
    public static final String rawSchemaParameter = "PrefixedRawSchema.@rawSchema";
    public static final String prefixParameter = "PrefixedRawSchema.@prefix";
    //endregion

    //region Constructors
    @Inject
    public PrefixedRawSchema(
            @Named(rawSchemaParameter) RawSchema rawSchema,
            @Named(prefixParameter) String prefix) {
        this.rawSchema = rawSchema;
        this.prefix = prefix;
    }
    //endregion

    //region RawSchema Implementation
    @Override
    public IndexPartitions getPartition(String type) {
        IndexPartitions indexPartitions = this.rawSchema.getPartition(type);

        if (indexPartitions.getPartitionField().isPresent()) {
            return new IndexPartitions.Impl(
                    indexPartitions.getPartitionField().get(),
                    Stream.ofAll(indexPartitions.getPartitions())
                            .map(this::prefixPartition));
        } else {
            return new IndexPartitions.Impl(
                    Stream.ofAll(indexPartitions.getPartitions())
                    .map(this::prefixPartition));
        }
    }

    @Override
    public String getIdFormat(String type) {
        return this.rawSchema.getIdFormat(type);
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return Stream.ofAll(this.rawSchema.getPartitions(type))
                .map(partition -> prefixPartition(partition)).toJavaList();
    }

    @Override
    public Iterable<String> indices() {
        return this.rawSchema.indices();
    }
    //endregion

    //region Private Methods
    private IndexPartitions.Partition prefixPartition(IndexPartitions.Partition partition) {
        if (Range.class.isAssignableFrom(partition.getClass())) {
            Range rangePartition = (Range)partition;
            return new Range.Impl(
                    rangePartition.getFrom(),
                    rangePartition.getTo(),
                    Stream.ofAll(rangePartition.getIndices()).map(index -> this.prefix + index));
        }

        return () -> Stream.ofAll(partition.getIndices()).map(index -> this.prefix + index);
    }
    //endregion

    //region Fields
    private RawSchema rawSchema;
    private String prefix;
    //endregion
}
