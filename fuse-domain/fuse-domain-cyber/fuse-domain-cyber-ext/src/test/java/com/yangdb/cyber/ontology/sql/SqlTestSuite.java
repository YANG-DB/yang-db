package com.yangdb.cyber.ontology.sql;

import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.test.BaseSuiteMarker;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.nio.file.Paths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticsearchFuseClientTest.class
})
public class SqlTestSuite implements BaseSuiteMarker {

    @BeforeClass
    public static void setup() throws Exception {
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("Dragons");
        startFuse();
    }

    public static void startFuse() {
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "resources", "conf", "application.engine2.dev.conf").toString()), "activeProfile");

        app.start("server.join=false");
    }

    @AfterClass
    public static void cleanup() {
        if (app != null) {
            app.stop();
        }
    }

    //region Fields
    private static Jooby app;
    public static ElasticEmbeddedNode elasticEmbeddedNode;
    //endregion
}
