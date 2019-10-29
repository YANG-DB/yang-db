package com.yangdb.fuse.graph;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.services.controller.PocGraphController;
import com.yangdb.fuse.services.controller.StandardPocGraphController;
import com.typesafe.config.Config;
import org.graphstream.graph.Graph;
import org.jooby.Env;

import java.time.Duration;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class PocModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        bindController(env, conf, binder);
    }

    private void bindController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<Cache<String, Graph>>() {})
                        .toInstance(Caffeine.newBuilder()
                                .expireAfterAccess(Duration.ofMinutes(10))
                                .maximumSize(10000)
                                .build());
                this.bind(PocGraphController.class)
                        .to(StandardPocGraphController.class);
                this.expose(PocGraphController.class);
            }
        });
    }
    //endregion
}
