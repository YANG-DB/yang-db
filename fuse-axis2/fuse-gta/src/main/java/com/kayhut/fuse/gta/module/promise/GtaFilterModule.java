package com.kayhut.fuse.gta.module.promise;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.gta.GtaTraversalCursorProcessor;
import com.kayhut.fuse.gta.translation.promise.M1FilterPlanTraversalTranslator;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class GtaFilterModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PlanTraversalTranslator.class).to(M1FilterPlanTraversalTranslator.class).asEagerSingleton();
        binder.bind(CursorCreationOperationContext.Processor.class).to(GtaTraversalCursorProcessor.class).asEagerSingleton();
    }
}
