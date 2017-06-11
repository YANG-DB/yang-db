package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Created by Roman on 07/06/2017.
 */
public class StamTest {
    @ClassRule
    public static JoobyRule createApp() {
        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"), new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine2.m1.test.conf", "m1.dfs.redundant"));
    }

    @Test
    public void testA() {
        int x = 5;
    }
}

