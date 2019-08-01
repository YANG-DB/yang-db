package com.yangdb.fuse.asg;

/*-
 * #%L
 * fuse-asg
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
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrar;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.jooby.Env;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AsgValidationModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        Class<? extends AsgValidatorStrategyRegistrar> asgValidatorStrategyRegistrar = AsgValidatorStrategyRegistrarImpl.class;
        try {
            asgValidatorStrategyRegistrar = getAsgValidatorStrategyRegistrar(conf);
        } catch (Throwable e) {
            //failed binding to external class - use default
        }

        binder.bind(AsgValidatorStrategyRegistrar.class)
                .to(asgValidatorStrategyRegistrar)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryValidator<AsgQuery>>() {})
                .to(AsgQueryValidator.class)
                .asEagerSingleton();
    }


    protected Class<? extends AsgValidatorStrategyRegistrar> getAsgValidatorStrategyRegistrar(Config conf) throws ClassNotFoundException {
        return (Class<? extends AsgValidatorStrategyRegistrar>) Class.forName(conf.getString(conf.getString("assembly") + ".asg_validator_strategy_registrar"));
    }
}
