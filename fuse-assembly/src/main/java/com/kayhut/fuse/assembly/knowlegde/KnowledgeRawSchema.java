package com.kayhut.fuse.assembly.knowlegde;

import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by lior.perry on 2/11/2018.
 */
public class KnowledgeRawSchema implements RawSchema {

    //region Static
    public static final String ENTITY = "entity";
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
