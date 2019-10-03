package com.yangdb.fuse.services.engine1;

import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.FuseRunner;
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
        CursorTest.class,
        DataTest.class,
        PageTest.class,
        QueryTest.class
})
public class TestSuite {
    @BeforeClass
    public static void setup() throws Exception {
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine1.dev.conf").toString()));
        //start app
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
    //endregion
}
