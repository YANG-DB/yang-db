package com.yangdb.fuse.services.engine2;

import com.yangdb.fuse.assembly.Setup;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.engine2.data.CsvCursorIT;
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
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CsvCursorIT.class
})
public class CsvCursorTestSuite implements BaseSuiteMarker {
    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("CsvCursorTestSuite start");
        Setup.withPath(Paths.get("src", "test", "conf", "application.engine2.dev.M2.discrete.conf"));
        Setup.activeProfile("m2.smartEpb");
        Setup.setup();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("CsvCursorTestSuite - teardown");
        Setup.cleanup();
    }


    public static ElasticEmbeddedNode elasticEmbeddedNode;
    //endregion
}
