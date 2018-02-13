package com.kayhut.fuse.services.module;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.dispatcher.driver.PageDriver;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.mock.elasticsearch.MockClient;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.services.dispatcher.driver.MockDriver;
import com.kayhut.fuse.services.engine2.data.schema.discrete.M2DragonsPhysicalSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.typesafe.config.Config;
import org.elasticsearch.client.Client;
import org.jooby.Env;

/**
 * Created by Roman on 04/04/2017.
 */
public class DriverTestModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(QueryDriver.class).to(MockDriver.Query.class).asEagerSingleton();
        binder.bind(CursorDriver.class).to(MockDriver.Cursor.class).asEagerSingleton();
        binder.bind(PageDriver.class).to(MockDriver.Page.class).asEagerSingleton();

        binder.bind(GraphElementSchemaProviderFactory.class)
                .toInstance(ontology -> new OntologySchemaProvider(ontology,new M2DragonsPhysicalSchemaProvider()));
        binder.bind(Client.class).toInstance(new MockClient());
    }

}
