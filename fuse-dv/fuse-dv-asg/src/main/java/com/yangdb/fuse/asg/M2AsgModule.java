package com.yangdb.fuse.asg;

/*-
 *
 * fuse-dv-asg
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.yangdb.fuse.asg.strategy.AsgStrategyRegistrar;
import com.yangdb.fuse.asg.strategy.CypherAsgStrategyRegistrar;
import com.yangdb.fuse.asg.strategy.M1CypherAsgStrategyRegistrar;
import com.yangdb.fuse.dispatcher.asg.QueryToCompositeAsgTransformer;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.Query;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by roman.margolis on 05/02/2018.
 */
public class M2AsgModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(CypherAsgStrategyRegistrar.class)
                .to(M1CypherAsgStrategyRegistrar.class)
                .asEagerSingleton();

        binder.bind(AsgStrategyRegistrar.class)
                .to(getAsgStrategyRegistrar(conf));

        binder.bind(new TypeLiteral<QueryTransformer<Query, AsgQuery>>(){})
                .to(QueryToCompositeAsgTransformer.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<String, AsgQuery>>(){})
                .to(AsgCypherTransformer.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<AsgQuery, AsgQuery>>(){})
                .to(AsgQueryTransformer.class);
    }
    //endregion


    protected Class<? extends AsgStrategyRegistrar> getAsgStrategyRegistrar(Config conf) throws ClassNotFoundException {
        return (Class<? extends  AsgStrategyRegistrar>)Class.forName(conf.getString(conf.getString("assembly")+".asg_strategy_registrar"));
    }
}
