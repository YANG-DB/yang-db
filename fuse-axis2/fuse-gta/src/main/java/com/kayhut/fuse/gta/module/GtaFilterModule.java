package com.kayhut.fuse.gta.module;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.gta.LoggingPlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.gta.translation.promise.M1FilterPlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class GtaFilterModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PlanTraversalTranslator.class)
                .annotatedWith(Names.named(LoggingPlanTraversalTranslator.injectionName))
                .to(M1FilterPlanTraversalTranslator.class)
                .asEagerSingleton();
        binder.bind(PlanTraversalTranslator.class)
                .to(LoggingPlanTraversalTranslator.class)
                .asEagerSingleton();
    }
    //endregion
}
