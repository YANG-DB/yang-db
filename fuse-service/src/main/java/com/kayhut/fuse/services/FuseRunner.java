package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.jooby.Jooby;

/**
 * Created by Roman on 05/06/2017.
 */
public class FuseRunner {
    public static void main(final String[] args) {
        final String applicationConfFilePath = args.length > 0 ?
                args[0] : "application.conf";

        final String activeProfile = args.length > 1 ?
                args[1] : "activeProfile";

        Jooby.run(() -> new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(applicationConfFilePath, activeProfile), args);
    }
}
