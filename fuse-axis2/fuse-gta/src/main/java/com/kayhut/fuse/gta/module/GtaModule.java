package com.kayhut.fuse.gta.module;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.gta.LoggingPlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

/**
 * Created by Roman on 23/05/2017.
 */
public class GtaModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        bindPlanTraversalTranslator(env, conf, binder);
    }
    //endregion

    //region Private Methods
    private void bindPlanTraversalTranslator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(PlanTraversalTranslator.class)
                            .annotatedWith(named(LoggingPlanTraversalTranslator.planTraversalTranslatorParameter))
                            .to(getPlanTraversalTranslatorClass(conf))
                            .asEagerSingleton();

                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingPlanTraversalTranslator.loggerParameter))
                            .toInstance(LoggerFactory.getLogger(getPlanTraversalTranslatorClass(conf)));

                    this.bind(PlanTraversalTranslator.class)
                            .to(LoggingPlanTraversalTranslator.class)
                            .asEagerSingleton();

                    this.expose(PlanTraversalTranslator.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Class<? extends PlanTraversalTranslator> getPlanTraversalTranslatorClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  PlanTraversalTranslator>)Class.forName(conf.getString("fuse.plan_traversal_translator_class"));
    }
    //endregion
}
