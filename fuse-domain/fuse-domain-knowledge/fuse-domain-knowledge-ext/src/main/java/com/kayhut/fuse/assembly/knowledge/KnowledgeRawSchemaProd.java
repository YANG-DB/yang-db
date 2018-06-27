package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class KnowledgeRawSchemaProd implements RawSchema {

    //region Static
    public static final String ENTITY = "entity";
    public static final String FILE = "file";
    public static final String ENTITY_VALUE = "e.value";
    public static final String RELATION = "relation";
    public static final String RELATION_VALUE = "r.value";
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
    public IndexPartitions getPartition(String type) {
        switch (type) {
            case ENTITY:
                return new IndexPartitions.Impl("logicalId", getPartitions(ENTITY));
            case ENTITY_VALUE:
                return new IndexPartitions.Impl("logicalId", getPartitions(ENTITY));
            case RELATION:
                return new IndexPartitions.Impl("_id", getPartitions(RELATION));
            case RELATION_VALUE:
                return new IndexPartitions.Impl("relationId", getPartitions(RELATION));
            case INSIGHT:
                return new IndexPartitions.Impl("_id", getPartitions(INSIGHT));
            case FILE:
                return new IndexPartitions.Impl("_id", getPartitions(ENTITY));
            case REFERENCE:
                return  new IndexPartitions.Impl("_id", getPartitions(REFERENCE));

        }
        return null;
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        switch (type) {
            case FILE:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("e000000000000", "e000010000000", "e-0"),
                        new IndexPartitions.Partition.Range.Impl<>("e000010000000", "e000020000000", "e-1"),
                        new IndexPartitions.Partition.Range.Impl<>("e000020000000", "e000030000000", "e-2"));
            case ENTITY:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("e000000000000", "e000010000000", "e-0"),
                        new IndexPartitions.Partition.Range.Impl<>("e000010000000", "e000020000000", "e-1"),
                        new IndexPartitions.Partition.Range.Impl<>("e000020000000", "e000030000000", "e-2"));
            case RELATION:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("r000000000000", "r000010000000", "rel-0"),
                        new IndexPartitions.Partition.Range.Impl<>("r000010000000", "r000020000000", "rel-1"),
                        new IndexPartitions.Partition.Range.Impl<>("r000020000000", "r000030000000", "rel-2"));
            case INSIGHT:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("i000000000000", "i000010000000", "i-0"),
                        new IndexPartitions.Partition.Range.Impl<>("i000010000000", "i000020000000", "i-1"),
                        new IndexPartitions.Partition.Range.Impl<>("i000020000000", "i000030000000", "i-2"));
            case REFERENCE:
                return Arrays.asList(
                        new IndexPartitions.Partition.Range.Impl<>("ref000000000000", "ref000010000000", "ref-0"),
                        new IndexPartitions.Partition.Range.Impl<>("ref000010000000", "ref000020000000", "ref-1"),
                        new IndexPartitions.Partition.Range.Impl<>("ref000020000000", "ref000030000000", "ref-2"));

        }
        return Collections.emptyList();
    }
    //endregion
}
