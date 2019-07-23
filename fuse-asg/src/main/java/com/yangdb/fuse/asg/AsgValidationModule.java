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
import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrar;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by lior.perry on 22/02/2017.
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
