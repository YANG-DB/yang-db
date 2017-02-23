package com.kayhut.fuse.dispatcher;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class DispatcherModule implements Jooby.Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //service controllers
        binder.bind(CursorDispatcherDriver.class).to(BaseCursorDispatcherDriver.class).asEagerSingleton();
        binder.bind(QueryDispatcherDriver.class).to(BaseQueryDispatcherDriver.class).asEagerSingleton();
    }

}
