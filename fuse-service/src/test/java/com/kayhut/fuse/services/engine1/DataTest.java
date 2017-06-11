package com.kayhut.fuse.services.engine1;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;

/**
 * Created by Roman on 04/04/2017.
 */
public class DataTest extends com.kayhut.fuse.services.mockEngine.DataTest {
    @ClassRule
    public static JoobyRule createApp() {
        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"), new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine1.dev.conf"));
    }
}
