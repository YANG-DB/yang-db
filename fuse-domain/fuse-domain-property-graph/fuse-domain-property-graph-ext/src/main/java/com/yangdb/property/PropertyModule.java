package com.yangdb.property;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.property.services.PropertyExtensionQueryController;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;

public class PropertyModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.bind(PropertyExtensionQueryController.class).in(RequestScoped.class);

    }
}
