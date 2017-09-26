package com.kayhut.fuse.gta.module.discrete;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.gta.GtaTraversalCursorProcessor;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.kayhut.fuse.gta.translation.discrete.M1PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by Roman on 23/05/2017.
 */
public class GtaModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PlanTraversalTranslator.class).to(M1PlanTraversalTranslator.class).asEagerSingleton();
        binder.bind(CursorCreationOperationContext.Processor.class).to(GtaTraversalCursorProcessor.class).asEagerSingleton();
    }
}
