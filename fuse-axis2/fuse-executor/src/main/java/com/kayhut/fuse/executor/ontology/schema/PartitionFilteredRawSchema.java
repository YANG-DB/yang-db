package com.kayhut.fuse.executor.ontology.schema;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;

import java.util.Collection;
import java.util.List;

/**
 * Created by roman.margolis on 01/03/2018.
 */
public class PartitionFilteredRawSchema implements RawSchema {
    public static final String rawSchemaParameter = "PartitionFilteredRawSchema.@rawSchema";

    //region Constructors
    @Inject
    public PartitionFilteredRawSchema(
            @Named(rawSchemaParameter) RawSchema rawSchema,
            Client client) {
        this.rawSchema = rawSchema;
        this.client = client;
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
                            .map(this::filterPartitionIndices));
        } else {
            return new IndexPartitions.Impl(
                    Stream.ofAll(indexPartitions.getPartitions())
                            .map(this::filterPartitionIndices));
        }
    }

    @Override
    public String getIdFormat(String type) {
        return this.rawSchema.getIdFormat(type);
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return Stream.ofAll(this.rawSchema.getPartitions(type))
                .map(partition -> filterPartitionIndices(partition)).toJavaList();
    }

    @Override
    public Iterable<String> indices() {
        return this.rawSchema.indices();
    }
    //endregion

    //region Private Methods
    private IndexPartitions.Partition filterPartitionIndices(IndexPartitions.Partition partition) {
        if (IndexPartitions.Partition.Range.class.isAssignableFrom(partition.getClass())) {
            IndexPartitions.Partition.Range rangePartition = (IndexPartitions.Partition.Range)partition;
            return new IndexPartitions.Partition.Range.Impl(
                    rangePartition.getFrom(),
                    rangePartition.getTo(),
                    filterIndices(rangePartition.getIndices()));
        }

        return () -> filterIndices(partition.getIndices());
    }

    private Iterable<String> filterIndices(Iterable<String> indices) {
        return Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .toJavaList();
    }
    //endregion

    //region Fields
    private RawSchema rawSchema;
    private Client client;
    //endregion
}
