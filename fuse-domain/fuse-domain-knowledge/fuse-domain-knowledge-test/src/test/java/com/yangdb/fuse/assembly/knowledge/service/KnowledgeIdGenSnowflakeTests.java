package com.yangdb.fuse.assembly.knowledge.service;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.services.controllers.IdGeneratorController.IDGENERATOR_INDEX;


@Ignore("Quartz Job: cant instansiate two (same) jobs within one scheduler")
public class KnowledgeIdGenSnowflakeTests {
    public static FuseApp app1 = null;
    public static FuseApp app2 = null;

    public static FuseClient fuseClient1 = null;
    public static FuseClient fuseClient2 = null;

    private static void createIdGeneratorIndex(Client client) {
        try {
            client.admin().indices().create(client.admin().indices().prepareCreate(IDGENERATOR_INDEX).request()).actionGet();
        }catch (ElasticsearchException err) {}

        Map<String, Object> doc = new HashMap<>();
        doc.put("value", 1);
        client.index(client.prepareIndex(IDGENERATOR_INDEX, "idsequence").setId("workerId").setSource(doc).request()).actionGet();
    }

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("KnowledgeIdGenSnowflakeTests - setup");

        // Start embedded ES
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("knowledge");
        createIdGeneratorIndex(ElasticEmbeddedNode.getClient());

        // Load fuse engine config file
        String confFilePath = Paths.get("resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf").toString();
        // Start elastic data manager
        client = elasticEmbeddedNode.getClient();
        manager = new KnowledgeConfigManager(confFilePath, client);
        // Connect to elastic
//        client = elasticEmbeddedNode.getClient("knowledge", 9300);
        // Create indexes by templates
        manager.init();

        // Start fuse app (based on Jooby app web server)
        app1 = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()),
                        "activeProfile");
        app1.start("server.join=false");
        //create fuse client class for web api access
        fuseClient1 = new BaseFuseClient("http://localhost:8888/fuse");

        // Start fuse app (based on Jooby app web server)
        app2 = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()),
                        "activeProfile", Tuple.of("application.port", ConfigValueFactory.fromAnyRef("8889")));
        app2.start("server.join=false");
        //create fuse client class for web api access
        fuseClient2 = new BaseFuseClient("http://localhost:8889/fuse");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("KnowledgeIdGenSnowflakeTests - teardown");
        client.admin().indices().delete(client.admin().indices().prepareDelete(IDGENERATOR_INDEX).request()).actionGet();
        app1.stop();
        app2.stop();
    }

    @Test
    public void testInsertOneSimpleEntityWithBuilder() throws IOException {
        Assert.assertEquals(1l, fuseClient1.getFuseSnowflakeId().longValue());
        Assert.assertEquals(2l, fuseClient2.getFuseSnowflakeId().longValue());
    }

}
