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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

/**
 * Created by roman.margolis on 06/03/2018.
 */
public class KnowledgeRawSchemaShort implements RawSchema {
    public static final String FUSE = "fuse";
    public static final String ID_FORMAT = "id_format";
    public static final String ID_BULK = "id_bulk";
    public static final String INDICES_COUNT = "indices_count";

    public static final String DEFAULT_ID_FORMAT = "%08d";
    public static final int DEFAULT_INDICES_COUNT = 10;
    public static final int ID_BULK_SIZE = 10000000;

    private String idFormat = DEFAULT_ID_FORMAT;
    private int indicesCount = DEFAULT_INDICES_COUNT;
    private long idBulk = ID_BULK_SIZE;


    //region Static
    public static final String ENTITY = "entity";
    public static final String EVALUE = "e.value";
    public static final String RELATION = "relation";
    public static final String RVALUE = "r.value";
    public static final String INSIGHT = "insight";
    public static final String REFERENCE = "reference";
    public static final String EFILE = "e.file";
    //endregion

    @Inject
    public KnowledgeRawSchemaShort(Config config) {
        try {
            idFormat = config.getString(format("%s.%s", FUSE,ID_FORMAT));
        }catch (Throwable t1) {
            //none found - using default
        };
        try {
            idBulk = config.getLong(format("%s.%s", FUSE,ID_BULK));
        }catch (Throwable t1) {
            //none found - using default
        };
        try {
            indicesCount =  config.getInt(format("%s.%s", FUSE,INDICES_COUNT));
        }catch (Throwable t1) {
            //none found - using default
        };

    }

    public KnowledgeRawSchemaShort() {}

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
                return idFormat;
            case RELATION:
                return idFormat;
            case INSIGHT:
                return idFormat;
            case REFERENCE:
                return idFormat;

        }
        return idFormat;
    }

    /**
     * Index name prefix
     */
    @Override
    public String getPrefix(String type) {
        switch (type) {
            case EFILE:
            case EVALUE:
            case ENTITY:
                return "e";
            case RELATION:
            case RVALUE:
                return "rel";
            case INSIGHT:
                return "i";
            case REFERENCE:
                return "ref";

        }
        return "e";
    }

    /**
     * id prefix
     * @param type
     * @return
     */
    @Override
    public String getIdPrefix(String type) {
        switch (type) {
            case EFILE:
                return "ef";
            case EVALUE:
                return "ev";
            case ENTITY:
                return "e";
            case RELATION:
                return "r";
            case RVALUE:
                return "rv";
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
                return IntStream.rangeClosed(0,indicesCount)
                        .mapToObj(e-> buildIndexPartition(e,ENTITY))
                        .collect(Collectors.toList());
            case RELATION:
                return IntStream.rangeClosed(0,indicesCount)
                        .mapToObj(r-> buildIndexPartition(r,RELATION))
                        .collect(Collectors.toList());
            case INSIGHT:
                return IntStream.rangeClosed(0,indicesCount)
                        .mapToObj(i-> buildIndexPartition(i,INSIGHT))
                        .collect(Collectors.toList());
            case REFERENCE:
                return IntStream.rangeClosed(0,indicesCount)
                        .mapToObj(ref-> buildIndexPartition(ref,REFERENCE))
                        .collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    public IndexPartitions.Partition.Range.Impl<String> buildIndexPartition(int index,String type) {
        return new IndexPartitions.Partition.Range.Impl<>(
                format("%s%s",getIdPrefix(type),format(getIdFormat(type),index*idBulk)),
                format("%s%s",getIdPrefix(type),format(getIdFormat(type),(index+1)*idBulk)),
                format("%s%d",getPrefix(type),index));
    }
    //endregion
}
