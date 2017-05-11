package com.kayhut.fuse.gta;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.gta.strategy.M1PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class GtaModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PlanTraversalTranslator.class).to(ChainedPlanOpTraversalTranslator.class).asEagerSingleton();
        binder.bind(PlanOpTranslationStrategy.class).to(M1PlanOpTranslationStrategy.class).asEagerSingleton();
        binder.bind(CursorCreationOperationContext.Processor.class).to(GtaTraversalCursorProcessor.class).asEagerSingleton();
    }
}
