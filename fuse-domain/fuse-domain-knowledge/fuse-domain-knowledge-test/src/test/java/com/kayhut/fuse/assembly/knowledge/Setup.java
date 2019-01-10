package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.kayhut.fuse.client.FuseClient;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class Setup {
    public static final Path path = Paths.get( "resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf");
    public static final String IDGENERATOR_INDEX = ".idgenerator";
    public static FuseApp app = null;
    public static ElasticEmbeddedNode elasticEmbeddedNode = null;
    public static KnowledgeConfigManager manager = null;
    public static FuseClient fuseClient = null;
    public static TransportClient client = null;

    public static void setup() throws Exception {
        setup(true);
    }

    public static void setup(boolean embedded) throws Exception {
        setup(embedded,true);
    }

    public static void setup(boolean embedded, boolean init) throws Exception {
        // Start embedded ES
        if(embedded) {
            elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("knowledge");
            client = elasticEmbeddedNode.getClient();
            createIdGeneratorIndex(client);
        } else {
            //use existing running ES
            client = elasticEmbeddedNode.getClient("knowledge", 9300);
        }
        // Load fuse engine config file
        String confFilePath = path.toString();
        // Start elastic data manager
        manager = new KnowledgeConfigManager(confFilePath, client);
        // Connect to elastic
        // Create indexes by templates
        if(init) {
            manager.init();
        }
        // Start fuse app (based on Jooby app web server)
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(path.toFile(), "activeProfile");
        app.start("server.join=false");
        //create fuse client class for web api access
        fuseClient = new FuseClient("http://localhost:8888/fuse");
    }

    public static void createIdGeneratorIndex(Client client) {
        client.admin().indices().create(client.admin().indices().prepareCreate(IDGENERATOR_INDEX).request()).actionGet();
        Map<String, Object> doc = new HashMap<>();
        doc.put("value", 1);
        client.index(client.prepareIndex(IDGENERATOR_INDEX, "idsequence").setId("workerId").setSource(doc).request()).actionGet();
    }

    public static void teardown() {
        client.admin().indices().delete(client.admin().indices().prepareDelete(IDGENERATOR_INDEX).request()).actionGet();
    }

    public static void cleanup() throws Exception {
        if (manager != null) {
            manager.drop();
        }
        if (app != null) {
            app.stop();
        }
    }
}
