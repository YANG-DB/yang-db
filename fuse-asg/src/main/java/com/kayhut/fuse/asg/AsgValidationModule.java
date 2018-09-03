package com.kayhut.fuse.asg;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.asg.validation.AsgQueryValidator;
import com.kayhut.fuse.asg.validation.AsgValidatorStrategyRegistrar;
import com.kayhut.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior on 22/02/2017.
 */
public class AsgValidationModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgValidatorStrategyRegistrar.class)
                .to(AsgValidatorStrategyRegistrarImpl.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryValidator<AsgQuery>>(){})
                .to(AsgQueryValidator.class)
                .asEagerSingleton();
    }
}
