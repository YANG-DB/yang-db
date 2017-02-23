package com.kayhut.fuse.services;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.BaseQueryDispatcherDriver;
import com.kayhut.fuse.dispatcher.QueryDispatcherDriver;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class ServicesModule implements Jooby.Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //service controllers
        binder.bind(QueryController.class).to(SimpleQueryController.class).asEagerSingleton();
        binder.bind(CursorController.class).to(SimpleCursorController.class).asEagerSingleton();
        binder.bind(ResultsController.class).to(SimpleResultsController.class).asEagerSingleton();
        binder.bind(SearchController.class).to(SimpleSearchController.class).asEagerSingleton();
    }

}
