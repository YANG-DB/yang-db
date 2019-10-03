package com.yangdb.fuse.services.mockEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.FuseRunner;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ApiDescriptorTest.class,
        CatalogTest.class,
        CursorTest.class,
        DataTest.class,
        PageTest.class,
        PlanTest.class,
        QueryTest.class,
        SearchTest.class
})
public class TestSuite {
    @BeforeClass
    public static void setup() throws Exception {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getNextResults(anyInt())).thenReturn(AssignmentsQueryResult.Builder.instance().build());

        CursorFactory cursorFactory = mock(CursorFactory.class);
        when(cursorFactory.createCursor(any())).thenReturn(cursor);

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.mockEngine.dev.conf").toString()))
                .injector((stage, module) -> Guice.createInjector(stage, Modules.override(module).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(CursorFactory.class).toInstance(cursorFactory);
                    }
                })));

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
