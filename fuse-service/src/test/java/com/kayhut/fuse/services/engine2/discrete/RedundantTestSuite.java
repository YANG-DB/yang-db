package com.kayhut.fuse.services.engine2.discrete;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.FuseRunner;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by roman.margolis on 02/10/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SingleEntityTest.class,
        EntityRelationEntityTest.class
})
public class RedundantTestSuite {
    @BeforeClass
    public static void setup() throws Exception {
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine3.dev.conf").toString()),
                        "m1.dfs.redundant");

        new FuseRunner().run(app, new FuseRunner.Options(Paths.get("src", "test", "conf", "logback.xml").toString(), false));
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (app != null) {
            app.stop();
        }

        if (elasticEmbeddedNode != null) {
            if (elasticEmbeddedNode.getClient() != null) {
                elasticEmbeddedNode.getClient().close();
            }

            elasticEmbeddedNode.close();
        }
    }

    //region Fields
    private static Jooby app;
    public static ElasticEmbeddedNode elasticEmbeddedNode;
    //endregion
}
