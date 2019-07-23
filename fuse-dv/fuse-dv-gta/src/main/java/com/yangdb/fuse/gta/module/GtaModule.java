package com.yangdb.fuse.gta.module;

/*-
 * #%L
 * fuse-dv-gta
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.yangdb.fuse.dispatcher.gta.LoggingPlanTraversalTranslator;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.gta.PlanTraversalTranslator;
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
