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
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

import java.util.List;

/**
 * Created by Roman on 20/04/2017.
 */
public abstract class ModuleBase implements Jooby.Module {
    //region Module Implementation
    @Override
    public void configure(Env env, Config config, Binder binder) throws Throwable {
        List<String> modules = config.getStringList("modules." + config.getString("application.profile"));
        if (modules.contains(this.getClass().getName())) {
            configureInner(env, config, binder);
        }
    }
    //endregion

    //region Abstract Methods
    protected abstract void configureInner(Env env, Config config, Binder binder) throws Throwable;
    //endregion
}
