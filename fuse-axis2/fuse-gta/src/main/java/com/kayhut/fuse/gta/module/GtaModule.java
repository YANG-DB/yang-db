package com.kayhut.fuse.gta.module;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.gta.LoggingPlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.gta.GtaTraversalCursorProcessor;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by Roman on 23/05/2017.
 */
public class GtaModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PlanTraversalTranslator.class)
                .annotatedWith(Names.named(LoggingPlanTraversalTranslator.injectionName))
                .to(getPlanTraversalTranslatorClass(conf))
                .asEagerSingleton();
        binder.bind(PlanTraversalTranslator.class)
                .to(LoggingPlanTraversalTranslator.class)
                .asEagerSingleton();


        binder.bind(CursorCreationOperationContext.Processor.class)
                .to(GtaTraversalCursorProcessor.class)
                .asEagerSingleton();
    }
    //endregion

    //region Private Methods
    private Class<? extends PlanTraversalTranslator> getPlanTraversalTranslatorClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  PlanTraversalTranslator>)Class.forName(conf.getString("fuse.plan_traversal_translator_class"));
    }
    //endregion
}
