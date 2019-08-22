package com.yangdb.Dragons;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.Dragons.services.DragonsExtensionQueryController;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;

public class DragonsModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.bind(DragonsExtensionQueryController.class).in(RequestScoped.class);

    }
}
