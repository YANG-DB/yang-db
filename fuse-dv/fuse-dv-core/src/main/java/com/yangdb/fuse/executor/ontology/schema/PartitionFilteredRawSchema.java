package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
            Provider<Client> client) {
        this.rawSchema = rawSchema;
        this.client = client;
    }
    //endregion

    @Override
    public IndexPartitions getPartition(Ontology ontology, String type) {
        return _getPartition(this.rawSchema.getPartition(ontology,type));
    }

    //region RawSchema Implementation
    @Override
    public IndexPartitions getPartition(String type) {
        return _getPartition(this.rawSchema.getPartition(type));
    }

    private IndexPartitions.Impl _getPartition(IndexPartitions indexPartitions) {
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
    public String getIndexPrefix(String type) {
        return rawSchema.getIndexPrefix(type);
    }

    @Override
    public String getIdPrefix(String type) {
        return rawSchema.getIdPrefix(type);
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
                //todo - verify why is this necessary ???
//                .filter(index -> client.get().admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .toJavaList();
    }
    //endregion

    //region Fields
    private RawSchema rawSchema;
    private Provider<Client> client;
    //endregion
}
