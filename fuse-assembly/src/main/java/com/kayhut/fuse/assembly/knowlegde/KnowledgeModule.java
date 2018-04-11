package com.kayhut.fuse.assembly.knowlegde;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.ontology.OntologyGraphElementSchemaProviderFactory;
import com.typesafe.config.Config;
import org.jooby.Env;

import static com.google.inject.name.Names.named;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class KnowledgeModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        String indexName = conf.getString(conf.getString("assembly")+".idGenerator_indexName");
        binder.bindConstant().annotatedWith(named(KnowledgeIdGenerator.indexNameParameter)).to(indexName);
        binder.bind(new TypeLiteral<IdGeneratorDriver<Object>>(){})
                .to(KnowledgeIdGenerator.class).asEagerSingleton();
    }
    //endregion
}
