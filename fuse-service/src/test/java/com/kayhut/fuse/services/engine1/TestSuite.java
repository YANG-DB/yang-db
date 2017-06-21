package com.kayhut.fuse.services.engine1;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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
    public static void setup() {
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine1.dev.conf");

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
