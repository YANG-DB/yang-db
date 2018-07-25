package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeDataInfraManager;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.kayhut.fuse.utils.FuseClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.test.framework.index.ElasticEmbeddedNode.getClient;

public abstract class Setup {
    public static final Path path = Paths.get( "resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf");
    public static final String IDGENERATOR_INDEX = ".idgenerator";
    public static FuseApp app = null;
    public static ElasticEmbeddedNode elasticEmbeddedNode = null;
    public static KnowledgeConfigManager manager = null;
    public static FuseClient fuseClient = null;
    public static TransportClient client = null;

    public static void setup() throws Exception {
        // Start embedded ES
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("knowledge");
        client = elasticEmbeddedNode.getClient();
        //client = getClient("knowledge", 9300);

        // Load fuse engine config file
        String confFilePath = path.toString();
        // Start elastic data manager
        manager = new KnowledgeConfigManager(confFilePath, client);
        // Connect to elastic
        // Create indexes by templates
        manager.init();
        // Start fuse app (based on Jooby app web server)
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(path.toFile(), "activeProfile");
        app.start("server.join=false");
        //create fuse client class for web api access
        fuseClient = new FuseClient("http://localhost:8888/fuse");
    }


    /*
    public static void teardown() {
        client.admin().indices().delete(client.admin().indices().prepareDelete(IDGENERATOR_INDEX).request()).actionGet();
    }*/

    public static void cleanup() throws Exception {
        if (manager != null) {
            manager.drop();
        }
        if (app != null) {
            app.stop();
        }
    }
}
