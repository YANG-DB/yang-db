package com.yangdb.fuse.assembly.knowledge;

/*-
 *
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lior.perry on 2/11/2018.
 */
public class KnowledgeRawSchema implements RawSchema {

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
                return "%012d";
            case RELATION:
                return "%012d";
            case INSIGHT:
                return "%012d";
            case REFERENCE:
                return "%012d";

        }
        return "%012d";
    }

    @Override
    public String getPrefix(String type) {
        switch (type) {
            case ENTITY:
                return "e";
            case RELATION:
                return "rel";
            case INSIGHT:
                return "i";
            case REFERENCE:
                return "ref";

        }
        return "e";
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
            case ENTITY:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("e000000000000", "e000010000000", "e0"),
                        new IndexPartitions.Partition.Range.Impl<>("e000010000000", "e000020000000", "e1"),
                        new IndexPartitions.Partition.Range.Impl<>("e000020000000", "e000030000000", "e2"));
            case RELATION:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("r000000000000", "r000010000000", "rel0"),
                        new IndexPartitions.Partition.Range.Impl<>("r000010000000", "r000020000000", "rel1"),
                        new IndexPartitions.Partition.Range.Impl<>("r000020000000", "r000030000000", "rel2"));
            case INSIGHT:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("i000000000000", "i000010000000", "i0"),
                        new IndexPartitions.Partition.Range.Impl<>("i000010000000", "i000020000000", "i1"),
                        new IndexPartitions.Partition.Range.Impl<>("i000020000000", "i000030000000", "i2"));
            case REFERENCE:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("ref000000000000", "ref000010000000", "ref0"),
                        new IndexPartitions.Partition.Range.Impl<>("ref000010000000", "ref000020000000", "ref1"),
                        new IndexPartitions.Partition.Range.Impl<>("ref000020000000", "ref000030000000", "ref2"));

        }
        return Collections.emptyList();
    }
    //endregion
}
