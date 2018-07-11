package com.kayhut.fuse.assembly.knowledge;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.kayhut.fuse.assembly.knowledge.cursor.KnowledgeGraphHierarchyTraversalCursor;
import com.kayhut.fuse.assembly.knowledge.logical.cursor.KnowledgeLogicalModelTraversalCursor;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.transport.cursor.CreateLogicalGraphHierarchyCursorRequest;
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
        String indexName = conf.getString(conf.getString("assembly") + ".idGenerator_indexName");
        binder.bindConstant().annotatedWith(named(KnowledgeIdGenerator.indexNameParameter)).to(indexName);
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>() {}).to(KnowledgeIdGenerator.class).asEagerSingleton();

        Multibinder<CompositeCursorFactory.Binding> bindingMultibinder = Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class);
        //KnowledgeGraphHierarchyCursor
        bindingMultibinder.addBinding().toInstance(new CompositeCursorFactory.Binding(
                KnowledgeGraphHierarchyCursorRequest.CursorType,
                KnowledgeGraphHierarchyCursorRequest.class,
                new KnowledgeGraphHierarchyTraversalCursor.Factory()));
        //KnowledgeLogicalModelTraversalCursor
        bindingMultibinder.addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreateLogicalGraphHierarchyCursorRequest.CursorType,
                CreateLogicalGraphHierarchyCursorRequest.class,
                new KnowledgeLogicalModelTraversalCursor.Factory()));
    }
    //endregion
}
