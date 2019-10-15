package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchemaShort.*;
import static java.lang.String.format;
import static java.util.stream.IntStream.rangeClosed;

public class KnowledgeRawSchemaShortTest {

    @Test
    public void testGetIndices(){
        KnowledgeRawSchemaShort rawSchemaShort = new KnowledgeRawSchemaShort();
        Stream<String> eS = rangeClosed(0, DEFAULT_INDICES_COUNT).mapToObj(e -> format("%s%d", "e", e));
        Stream<String> rS = rangeClosed(0,DEFAULT_INDICES_COUNT).mapToObj(r->format("%s%d","rel",r));
        Stream<String> iS = rangeClosed(0,DEFAULT_INDICES_COUNT).mapToObj(i->format("%s%d","i",i));
        Stream<String> refS = rangeClosed(0,DEFAULT_INDICES_COUNT).mapToObj(ref->format("%s%d","ref",ref));

        List<String> list = Stream.concat(Stream.concat(eS, rS),
                Stream.concat(refS,iS)).collect(Collectors.toList());
        Iterable<String> indices = rawSchemaShort.indices();
        Assert.assertEquals(list,indices);
    }

    @Test
    public void testGetPartitions(){
        KnowledgeRawSchemaShort rawSchemaShort = new KnowledgeRawSchemaShort();
        List<IndexPartitions.Partition> partitions = rawSchemaShort.getPartitions(ENTITY);

        KnowledgeRawSchemaShort finalRawSchemaShort = rawSchemaShort;
        List<IndexPartitions.Partition.Range.Impl<String>> list = rangeClosed(0, DEFAULT_INDICES_COUNT)
                .mapToObj(e -> finalRawSchemaShort.buildIndexPartition(e, ENTITY))
                .collect(Collectors.toList());

        Assert.assertEquals(list,partitions);

        rawSchemaShort = new KnowledgeRawSchemaShort();
        partitions = rawSchemaShort.getPartitions(RELATION);

        KnowledgeRawSchemaShort finalRawSchemaShort1 = rawSchemaShort;
        list = rangeClosed(0, DEFAULT_INDICES_COUNT)
                .mapToObj(e -> finalRawSchemaShort1.buildIndexPartition(e, RELATION))
                .collect(Collectors.toList());

        Assert.assertEquals(list,partitions);

        rawSchemaShort = new KnowledgeRawSchemaShort();
        partitions = rawSchemaShort.getPartitions(INSIGHT);

        KnowledgeRawSchemaShort finalRawSchemaShort2 = rawSchemaShort;
        list = rangeClosed(0, DEFAULT_INDICES_COUNT)
                .mapToObj(e -> finalRawSchemaShort2.buildIndexPartition(e, INSIGHT))
                .collect(Collectors.toList());

        Assert.assertEquals(list,partitions);

        rawSchemaShort = new KnowledgeRawSchemaShort();
        partitions = rawSchemaShort.getPartitions(REFERENCE);

        KnowledgeRawSchemaShort finalRawSchemaShort3 = rawSchemaShort;
        list = rangeClosed(0, DEFAULT_INDICES_COUNT)
                .mapToObj(e -> finalRawSchemaShort3.buildIndexPartition(e, REFERENCE))
                .collect(Collectors.toList());

        Assert.assertEquals(list,partitions);
        }
}
