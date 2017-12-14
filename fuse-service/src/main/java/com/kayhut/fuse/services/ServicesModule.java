package com.kayhut.fuse.services;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.kayhut.fuse.services.controllers.*;
import com.kayhut.fuse.services.controllers.logging.*;
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
        binder.bind(ApiDescriptionController.class).annotatedWith(Names.named(LoggingApiDescriptionController.injectionName)).to(StandardApiDescriptionController.class).asEagerSingleton();
        binder.bind(ApiDescriptionController.class).to(LoggingApiDescriptionController.class).asEagerSingleton();

        binder.bind(QueryController.class).annotatedWith(Names.named(LoggingQueryController.injectionName)).to(StandardQueryController.class).asEagerSingleton();
        binder.bind(QueryController.class).to(LoggingQueryController.class).asEagerSingleton();

        binder.bind(CursorController.class).annotatedWith(Names.named(LoggingCursorController.injectionName)).to(StandardCursorController.class).asEagerSingleton();
        binder.bind(CursorController.class).to(LoggingCursorController.class).asEagerSingleton();

        binder.bind(PageController.class).annotatedWith(Names.named(LoggingPageController.injectionName)).to(StandardPageController.class).asEagerSingleton();
        binder.bind(PageController.class).to(LoggingPageController.class).asEagerSingleton();

        binder.bind(SearchController.class).annotatedWith(Names.named(LoggingSearchController.injectionName)).to(StandardSearchController.class).asEagerSingleton();
        binder.bind(SearchController.class).to(LoggingSearchController.class).asEagerSingleton();

        binder.bind(CatalogController.class).annotatedWith(Names.named(LoggingCatalogController.injectionName)).to(StandardCatalogController.class).asEagerSingleton();
        binder.bind(CatalogController.class).to(LoggingCatalogController.class).asEagerSingleton();
    }

}
