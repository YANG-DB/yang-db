package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeDataInfraManager;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.kayhut.fuse.utils.FuseClient;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;


public class KnowledgeIdGenSnowflakeTests {
    public static FuseApp app1 = null;
    public static FuseApp app2 = null;

    public static FuseClient fuseClient1 = null;
    public static FuseClient fuseClient2 = null;

    @BeforeClass
    public static void setup() throws Exception {
        // Start embedded ES
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("knowledge");

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
        fuseClient1 = new FuseClient("http://localhost:8888/fuse");

        // Start fuse app (based on Jooby app web server)
        app2 = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()),
                        "activeProfile", Tuple.of("application.port", ConfigValueFactory.fromAnyRef("8889")));
        app2.start("server.join=false");
        //create fuse client class for web api access
        fuseClient2 = new FuseClient("http://localhost:8889/fuse");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        client.admin().indices().delete(client.admin().indices().prepareDelete(Setup.IDGENERATOR_INDEX).request()).actionGet();
        app1.stop();
        app2.stop();
    }

    @Test
    public void testInsertOneSimpleEntityWithBuilder() throws IOException {
        Assert.assertEquals(1l, fuseClient1.getFuseSnowflakeId().longValue());
        Assert.assertEquals(2l, fuseClient2.getFuseSnowflakeId().longValue());
    }

}
