package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javaslang.collection.Stream;
import org.jooby.Jooby;

/**
 * Created by Roman on 05/06/2017.
 */
public class FuseRunner {
    public static void main(final String[] args) {
        System.out.println("Args:");
        Stream.of(args).forEach(System.out::println);

        final String applicationConfFilePath = args.length > 0 ?
                args[0] : "application.conf";

        final String activeProfile = args.length > 1 ?
                args[1] : "activeProfile";

        Jooby.run(() -> new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(applicationConfFilePath, activeProfile), args);
    }
}
