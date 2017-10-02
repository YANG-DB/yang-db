package com.kayhut.fuse.dispatcher;

import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.processor.PageProcessor;
import com.kayhut.fuse.dispatcher.context.processor.ResourcePersistProcessor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.driver.*;
import com.kayhut.fuse.dispatcher.interception.ExceptionHandlingMethodInterceptor;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.InMemoryResourceStore;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.typesafe.config.Config;
import org.jooby.Env;

import static com.kayhut.fuse.model.Utils.baseUrl;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class DispatcherModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AppUrlSupplier.class).toInstance(new DefaultAppUrlSupplier(conf.getString("appUrlSupplier.public.baseUri")));

        // resource store and persist processor
        binder.bind(ResourceStore.class).to(InMemoryResourceStore.class).asEagerSingleton();
        binder.bind(OntologyProvider.class).to(getOntologyProviderClass(conf)).asEagerSingleton();
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
    private Class<? extends OntologyProvider> getOntologyProviderClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  OntologyProvider>)Class.forName(conf.getString("fuse.ontology_provider"));
    }
    //endregion
}
