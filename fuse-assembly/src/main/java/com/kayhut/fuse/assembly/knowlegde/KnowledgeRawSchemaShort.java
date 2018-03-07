package com.kayhut.fuse.assembly.knowlegde;

import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
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
