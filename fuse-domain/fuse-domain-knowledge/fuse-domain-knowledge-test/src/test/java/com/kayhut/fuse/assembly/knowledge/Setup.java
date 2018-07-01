package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeDataInfraManager;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.utils.FuseClient;
import com.kayhut.fuse.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.elasticsearch.client.transport.TransportClient;
import org.jooby.Jooby;

import java.io.File;
import java.nio.file.Paths;

public abstract class Setup {
    public static Jooby app = null;
    public static ElasticEmbeddedNode elasticEmbeddedNode = null;
    public static KnowledgeDataInfraManager manager = null;
    public static FuseClient fuseClient = null;
    public static TransportClient client = null;

    public static void setup() throws Exception {
        // Start embedded ES
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("knowledge");
        // Load fuse engine config file
        String confFilePath = Paths.get("resources","assembly","Knowledge","config", "application.test.engine3.m1.dfs.knowledge.public.conf").toString();
        // Start elastic data manager
        manager = new KnowledgeDataInfraManager(confFilePath);
        // Connect to elastic
//        client = elasticEmbeddedNode.getClient("knowledge", 9300);
        client = elasticEmbeddedNode.getClient();
        // Create indexes by templates
        manager.init(client);
        // Start fuse app (based on Jooby app web server)
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("resources","assembly","Knowledge","config", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()),
                        "activeProfile");
        app.start("server.join=false");
        //create fuse client class for web api access
        fuseClient = new FuseClient("http://localhost:8888/fuse");
    }

    public static void cleanup() throws Exception {
        if(manager != null) {
            manager.drop();
        }
        if(app != null){
            app.stop();
        }
    }

}
