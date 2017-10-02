package com.kayhut.fuse.gta.module;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.gta.GtaTraversalCursorProcessor;
import com.kayhut.fuse.gta.translation.promise.M1PlanTraversalTranslator;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by Roman on 23/05/2017.
 */
public class GtaModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PlanTraversalTranslator.class).to(getPlanTraversalTranslatorClass(conf)).asEagerSingleton();
        binder.bind(CursorCreationOperationContext.Processor.class).to(GtaTraversalCursorProcessor.class).asEagerSingleton();
    }

    private Class<? extends PlanTraversalTranslator> getPlanTraversalTranslatorClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  PlanTraversalTranslator>)Class.forName(conf.getString("fuse.plan_traversal_translator_class"));
    }
}
