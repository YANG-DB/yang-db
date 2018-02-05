package com.kayhut.fuse.asg;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.M1AsgStrategyRegistrar;
import com.kayhut.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Query;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by roman.margolis on 05/02/2018.
 */
public class AsgModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgStrategyRegistrar.class)
                .to(M1AsgStrategyRegistrar.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<Query, AsgQuery>>(){})
                .to(QueryToAsgTransformer.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<AsgQuery, AsgQuery>>(){})
                .to(AsgQueryTransformer.class)
                .asEagerSingleton();
    }
    //endregion
}
