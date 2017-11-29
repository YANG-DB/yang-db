package com.kayhut.fuse.dispatcher.modules;

import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.processor.PageProcessor;
import com.kayhut.fuse.dispatcher.context.processor.ResourcePersistProcessor;
import com.kayhut.fuse.dispatcher.driver.*;
import com.kayhut.fuse.dispatcher.interception.ExceptionHandlingMethodInterceptor;
import com.kayhut.fuse.dispatcher.ontology.DirectoryOntologyProvider;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.store.InMemoryResourceStore;
import com.kayhut.fuse.dispatcher.resource.store.LoggingResourceStore;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class NewDispatcherModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AppUrlSupplier.class).toInstance(new DefaultAppUrlSupplier(conf.getString("appUrlSupplier.public.baseUri")));

        // resource store and persist processor
        binder.bind(ResourceStore.class)
                .annotatedWith(Names.named(LoggingResourceStore.injectionName))
                .to(InMemoryResourceStore.class)
                .asEagerSingleton();
        binder.bind(ResourceStore.class)
                .to(LoggingResourceStore.class)
                .asEagerSingleton();

        binder.bind(OntologyProvider.class).toInstance(getOntologyProvider(conf));
        binder.bind(ResourcePersistProcessor.class).asEagerSingleton();

        // page processor
        binder.bind(PageCreationOperationContext.Processor.class).to(PageProcessor.class).asEagerSingleton();

        // service controllers
        binder.bind(QueryDispatcherDriver.class).to(SimpleQueryDispatcherDriver.class).asEagerSingleton();
        binder.bind(CursorDispatcherDriver.class).to(SimpleCursorDispatcherDriver.class).asEagerSingleton();
        binder.bind(PageDispatcherDriver.class).to(SimplePageDispatcherDriver.class).asEagerSingleton();

        // inteceptors

        binder.bindInterceptor(
                Matchers.subclassesOf(QueryCreationOperationContext.Processor.class),
                Matchers.any(),
                new ExceptionHandlingMethodInterceptor());

        binder.bindInterceptor(
                Matchers.subclassesOf(CursorCreationOperationContext.Processor.class),
                Matchers.any(),
                new ExceptionHandlingMethodInterceptor());

        binder.bindInterceptor(
                Matchers.subclassesOf(PageCreationOperationContext.Processor.class),
                Matchers.any(),
                new ExceptionHandlingMethodInterceptor());
    }

    //region Private Methods
    private OntologyProvider getOntologyProvider(Config conf) throws ClassNotFoundException {
        return new DirectoryOntologyProvider(conf.getString("fuse.ontology_provider_dir"));
    }
    //endregion
}
