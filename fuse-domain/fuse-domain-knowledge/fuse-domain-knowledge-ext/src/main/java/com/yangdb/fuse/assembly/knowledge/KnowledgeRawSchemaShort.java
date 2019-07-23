package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by roman.margolis on 06/03/2018.
 */
public class KnowledgeRawSchemaShort implements RawSchema {

    //region Static
    public static final String ENTITY = "entity";
    public static final String EFILE = "e.file";
    public static final String EVALUE = "e.value";
    public static final String RELATION = "relation";
    public static final String RVALUE = "r.value";
    public static final String INSIGHT = "insight";
    public static final String REFERENCE = "reference";
    //endregion

    //region RawSchema
    @Override
    public Iterable<String> indices() {

        Iterable<String> allIndices = Stream.ofAll(getPartitions(ENTITY))
                .appendAll(getPartitions(RELATION))
                .appendAll(getPartitions(REFERENCE))
                .appendAll(getPartitions(INSIGHT))
                .flatMap(IndexPartitions.Partition::getIndices).distinct().toJavaList();
        return allIndices;
    }

    @Override
    public String getIdFormat(String type) {
        switch (type) {
            case ENTITY:
                return "%08d";
            case RELATION:
                return "%08d";
            case INSIGHT:
                return "%08d";
            case REFERENCE:
                return "%08d";

        }
        return "%08d";
    }

    @Override
    public IndexPartitions getPartition(String type) {
        switch (type) {
            case ENTITY:
                return new IndexPartitions.Impl("logicalId", getPartitions(ENTITY));
            case EVALUE:
                return new IndexPartitions.Impl("logicalId", getPartitions(ENTITY));
            case RELATION:
                return new IndexPartitions.Impl("_id", getPartitions(RELATION));
            case RVALUE:
                return new IndexPartitions.Impl("relationId", getPartitions(RELATION));
            case INSIGHT:
                return new IndexPartitions.Impl("_id", getPartitions(INSIGHT));
            case EFILE:
                return new IndexPartitions.Impl("logicalId", getPartitions(ENTITY));
            case REFERENCE:
                return  new IndexPartitions.Impl("_id", getPartitions(REFERENCE));

        }
        return null;
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        switch (type) {
            case ENTITY :
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("e00000000", "e10000000", "e0"),
                        new IndexPartitions.Partition.Range.Impl<>("e10000000", "e20000000", "e1"),
                        new IndexPartitions.Partition.Range.Impl<>("e20000000", "e30000000", "e2"));
            case RELATION:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("r00000000", "r10000000", "rel0"),
                        new IndexPartitions.Partition.Range.Impl<>("r10000000", "r20000000", "rel1"),
                        new IndexPartitions.Partition.Range.Impl<>("r20000000", "r30000000", "rel2"));
            case INSIGHT:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("i00000000", "i10000000", "i0"),
                        new IndexPartitions.Partition.Range.Impl<>("i10000000", "i20000000", "i1"),
                        new IndexPartitions.Partition.Range.Impl<>("i20000000", "i30000000", "i2"));
            case REFERENCE:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("ref00000000", "ref10000000", "ref0"),
                        new IndexPartitions.Partition.Range.Impl<>("ref10000000", "ref20000000", "ref1"),
                        new IndexPartitions.Partition.Range.Impl<>("ref20000000", "ref30000000", "ref2"));

        }
        return Collections.emptyList();
    }
    //endregion
}
