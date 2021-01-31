package com.yangdb.fuse.asg;

/*-
 * #%L
 * fuse-dv-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.typesafe.config.Config;
import com.yangdb.fuse.asg.strategy.AsgStrategyRegistrar;
import com.yangdb.fuse.asg.translator.BasicJsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import org.jooby.Env;

/**
 * Created by roman.margolis on 05/02/2018.
 */
public class M2AsgModule extends AsgModule {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        super.configureInner(env,conf,binder);
        //configure the different query languages transformer factory
        binder.bind(JsonQueryTransformerFactory.class).to(BasicJsonQueryTransformerFactory.class);
    }
    //endregion


    protected Class<? extends AsgStrategyRegistrar> getAsgStrategyRegistrar(Config conf) throws ClassNotFoundException {
        return (Class<? extends  AsgStrategyRegistrar>)Class.forName(conf.getString(conf.getString("assembly")+".asg_strategy_registrar"));
    }
}
