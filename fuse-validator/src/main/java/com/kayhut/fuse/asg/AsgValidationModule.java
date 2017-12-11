package com.kayhut.fuse.asg;

import com.google.inject.Binder;
import com.kayhut.fuse.asg.strategy.AsgValidatorStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.AsgValidatorStrategyRegistrarImpl;
import com.kayhut.fuse.asg.strategy.ValidatorStrategyRegisteredAsgDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.context.QueryValidationOperationContext;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class AsgValidationModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgValidatorStrategyRegistrar.class).to(AsgValidatorStrategyRegistrarImpl.class).asEagerSingleton();
        binder.bind(QueryValidationOperationContext.Processor.class).to(ValidatorStrategyRegisteredAsgDriver.class).asEagerSingleton();
    }
}
