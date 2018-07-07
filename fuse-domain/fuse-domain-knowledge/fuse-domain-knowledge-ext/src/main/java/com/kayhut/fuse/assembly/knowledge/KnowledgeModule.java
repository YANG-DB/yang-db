package com.kayhut.fuse.assembly.knowledge;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.CursorBindingModule;
import com.kayhut.fuse.executor.cursor.discrete.PathsTraversalCursor;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;

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
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>(){})
                .to(KnowledgeIdGenerator.class).asEagerSingleton();

        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                KnowledgeGraphHierarchyCursorRequest.CursorType,
                KnowledgeGraphHierarchyCursorRequest.class,
                new KnowledgeGraphHierarchyTraversalCursor.Factory()));
    }
    //endregion
}
