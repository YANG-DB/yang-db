package com.kayhut.fuse.asg;

import com.google.inject.Binder;
import com.kayhut.fuse.asg.strategy.AsgDefaultStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.SimpleStrategyRegisteredAsgDriver;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class AsgModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgStrategyRegistrar.class).to(AsgDefaultStrategyRegistrar.class).asEagerSingleton();
        binder.bind(SimpleStrategyRegisteredAsgDriver.class).asEagerSingleton();
    }
}
