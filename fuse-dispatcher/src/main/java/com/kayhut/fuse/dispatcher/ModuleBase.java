package com.kayhut.fuse.dispatcher;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

import java.util.List;

/**
 * Created by Roman on 20/04/2017.
 */
public abstract class ModuleBase implements Jooby.Module {
    //region Module Implementation
    @Override
    public void configure(Env env, Config config, Binder binder) throws Throwable {
        List<String> modules = config.getStringList("modules." + config.getString("application.profile"));
        if (modules.contains(this.getClass().getName())) {
            configureInner(env, config, binder);
        }
    }
    //endregion

    //region Abstract Methods
    protected abstract void configureInner(Env env, Config config, Binder binder) throws Throwable;
    //endregion
}
