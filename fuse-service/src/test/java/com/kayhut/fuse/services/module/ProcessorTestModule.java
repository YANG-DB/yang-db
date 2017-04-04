package com.kayhut.fuse.services.module;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.context.processor.ResourcePersistProcessor;
import com.kayhut.fuse.dispatcher.driver.*;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.InMemoryResourceStore;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.dispatcher.context.processor.QueryCursorPageTestProcessor;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

import static com.kayhut.fuse.model.Utils.baseUrl;

/**
 * Created by Roman on 04/04/2017.
 */
public class ProcessorTestModule  implements Jooby.Module {
    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(QueryCursorPageTestProcessor.class).asEagerSingleton();
    }

}
