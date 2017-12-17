package com.kayhut.fuse.services;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.*;
import com.kayhut.fuse.services.controllers.logging.*;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;
import org.jooby.scope.RequestScoped;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class ServicesModule implements Jooby.Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //service controllers
        binder.bind(ApiDescriptionController.class)
                .annotatedWith(Names.named(LoggingApiDescriptionController.injectionName))
                .to(StandardApiDescriptionController.class);
        binder.bind(ApiDescriptionController.class)
                .to(LoggingApiDescriptionController.class);

        binder.bind(QueryController.class)
                .annotatedWith(Names.named(LoggingQueryController.injectionName))
                .to(StandardQueryController.class);
        binder.bind(QueryController.class)
                .to(LoggingQueryController.class);

        binder.bind(CursorController.class)
                .annotatedWith(Names.named(LoggingCursorController.injectionName))
                .to(StandardCursorController.class);
        binder.bind(CursorController.class)
                .to(LoggingCursorController.class);

        binder.bind(PageController.class)
                .annotatedWith(Names.named(LoggingPageController.injectionName))
                .to(StandardPageController.class);
        binder.bind(PageController.class)
                .to(LoggingPageController.class);

        binder.bind(SearchController.class)
                .annotatedWith(Names.named(LoggingSearchController.injectionName))
                .to(StandardSearchController.class);
        binder.bind(SearchController.class)
                .to(LoggingSearchController.class);

        binder.bind(CatalogController.class)
                .annotatedWith(Names.named(LoggingCatalogController.injectionName))
                .to(StandardCatalogController.class);
        binder.bind(CatalogController.class)
                .to(LoggingCatalogController.class);


        // bind requests
        binder.bind(CreateQueryRequest.class).in(RequestScoped.class);
        binder.bind(CreateCursorRequest.class).in(RequestScoped.class);
        binder.bind(CreatePageRequest.class).in(RequestScoped.class);
    }
}
