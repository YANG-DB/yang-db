package com.yangdb.fuse.services.engine2.discrete;

import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.FuseRunner;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.test.BaseSuiteMarker;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
public class RedundantTestSuite implements BaseSuiteMarker {
    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("RedundantTestSuite start");
        start = System.currentTimeMillis();

        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("Dragons");

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine3.dev.conf").toString()),
                        "m1.dfs.redundant");

        app.start("server.join=false");

    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (app != null) {
            app.stop();
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("RedundantTestSuite elapsed: " + elapsed);
    }

    //region Fields
    private static long start;
    private static Jooby app;
    public static ElasticEmbeddedNode elasticEmbeddedNode;
    //endregion
}
