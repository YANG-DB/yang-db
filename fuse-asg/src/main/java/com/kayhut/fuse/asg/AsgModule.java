package com.kayhut.fuse.asg;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.CypherAsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.M1AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.M1CypherAsgStrategyRegistrar;
import com.kayhut.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Query;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AsgModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgStrategyRegistrar.class)
                .to(M1AsgStrategyRegistrar.class)
                .asEagerSingleton();

        binder.bind(CypherAsgStrategyRegistrar.class)
                .to(M1CypherAsgStrategyRegistrar.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<Query, AsgQuery>>(){})
                .to(QueryToAsgTransformer.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<String, AsgQuery>>(){})
                .to(AsgCypherTransformer.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<AsgQuery, AsgQuery>>(){})
                .to(AsgQueryTransformer.class)
                .asEagerSingleton();
    }
}
