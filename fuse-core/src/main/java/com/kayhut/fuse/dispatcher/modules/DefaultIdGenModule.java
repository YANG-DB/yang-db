package com.kayhut.fuse.dispatcher.modules;

/*-
 * #%L
 * fuse-core
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
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.Range;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class DefaultIdGenModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) {
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>(){}).toInstance((genName, numIds) -> new Range(1l,2l));
    }
}
