package com.kayhut.fuse.asg;

import com.google.inject.Binder;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.M1AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.AsgQueryProcessor;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class AsgModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgStrategyRegistrar.class).to(M1AsgStrategyRegistrar.class).asEagerSingleton();
        binder.bind(AsgQueryProcessor.class).asEagerSingleton();
    }
}
