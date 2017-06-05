package com.kayhut.test.runner;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.FuseApp;
import org.jooby.Jooby;

/**
 * Created by liorp on 6/5/2017.
 */
public class M1DragonRunner {

    public static void main(final String[] args) {
        Jooby.run(() -> new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine2.m1.staging.conf", "m1.dfs.redundant"), args);
    }


}
