package com.kayhut.fuse.services;

import com.google.inject.Binder;
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
        binder.bind(PageController.class).to(SimplePageController.class).asEagerSingleton();
        binder.bind(SearchController.class).to(SimpleSearchController.class).asEagerSingleton();
        binder.bind(CatalogController.class).to(SimpleCatalogController.class).asEagerSingleton();
    }

}
