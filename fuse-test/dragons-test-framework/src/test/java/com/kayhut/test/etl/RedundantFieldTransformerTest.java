package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.MappingElasticConfigurer;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import com.kayhut.test.framework.providers.FileCsvDataProvider;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.util.*;

/**
 * Created by moti on 6/5/2017.
 */
public class RedundantFieldTransformerTest {

    @Test
    @Ignore
    public void test() throws Exception {
        /*ElasticEmbeddedNode embeddedNode = new ElasticEmbeddedNode(new MappingFileElasticConfigurer("dragons", "src\\test\\resources\\dragon_mapping.json"));
        FileCsvDataProvider csvDataProvider = new FileCsvDataProvider("C:\\Users\\moti\\Downloads\\data\\dragonsTEST.csv",
                CsvSchema.builder().addColumn("id").addColumn("name").addColumn("age").addColumn("gender").addColumn("color").build());
        ElasticDataPopulator populator = new ElasticDataPopulator(embeddedNode.getClient(), "dragons","dragon","id", csvDataProvider);
        populator.populate();*/

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "fuse-test")
                .build();

        TransportClient transportClient = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.81.12.209"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.73.165.97"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("52.166.57.208"), 9300));


        Map<String, String> fields = new HashMap<>();
        fields.put("name", "entityB.name");

        //RedundantFieldTransformer transformer = new RedundantFieldTransformer(transportClient, fields, "EntityB.Id", "id", Arrays.asList("dragons"), "Dragon");

        List<Map<String,String>> documents = new ArrayList<>();
        Map<String, String> doc = new HashMap<>();

        doc.put("id","Fire123");
        doc.put("EntityA.Id","Dragon__7");
        doc.put("EntityB.Id","Dragon_1");
        doc.put("time", "10000");
        doc.put("duration","10");
        documents.add(doc);

        //transformer.transform(documents);
    }
}

