package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import com.kayhut.test.framework.providers.FileCsvDataProvider;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * Created by moti on 6/5/2017.
 */
public class RedundantFieldTransformerTest {

    @Test
    @Ignore
    public void test() throws Exception {
        ElasticEmbeddedNode embeddedNode = new ElasticEmbeddedNode();
        FileCsvDataProvider csvDataProvider = new FileCsvDataProvider("C:\\Users\\moti\\Downloads\\data\\dragonsTEST.csv",
                CsvSchema.builder().addColumn("id").addColumn( "name").addColumn("age").addColumn("gender").addColumn("color").build());
        ElasticDataPopulator populator = new ElasticDataPopulator(embeddedNode.getClient(), "dragons","dragon","id", csvDataProvider);
        populator.populate();

        Map<String, String> fields = new HashMap<>();
        fields.put("name", "entityB.name");

        RedundantFieldTransformer transformer = new RedundantFieldTransformer(embeddedNode.getClient(), fields, "EntityB.Id", "id", Arrays.asList("dragons"), "dragon");

        List<Map<String,String>> documents = new ArrayList<>();
        Map<String, String> doc = new HashMap<>();

        doc.put("id","Fire123");
        doc.put("EntityA.Id","0");
        doc.put("EntityB.Id","1");
        doc.put("time", "10000");
        doc.put("duration","10");
        documents.add(doc);

        transformer.transform(documents);
    }
}

