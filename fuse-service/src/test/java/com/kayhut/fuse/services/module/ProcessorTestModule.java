package com.kayhut.fuse.services.module;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.mock.elasticsearch.MockClient;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.services.dispatcher.context.processor.QueryCursorPageTestProcessor;
import com.typesafe.config.Config;
import org.elasticsearch.client.Client;
import org.jooby.Env;

/**
 * Created by Roman on 04/04/2017.
 */
public class ProcessorTestModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(QueryCursorPageTestProcessor.class).asEagerSingleton();
        binder.bind(GraphElementSchemaProviderFactory.class).toInstance(ontology -> null);
        binder.bind(Client.class).toInstance(new MockClient());
    }

}
