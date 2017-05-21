package com.kayhut.fuse.services.engine2;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * Created by Roman on 04/04/2017.
 */
public class QueryTest extends com.kayhut.fuse.services.mockEngine.QueryTest {
    @ClassRule
    public static JoobyRule createApp() {
        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine2.dev.conf", "m1.dfs.non_redundant"));
    }
}
