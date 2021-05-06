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
import com.google.inject.name.Named;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions.Partition.Range;
import javaslang.collection.Stream;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
