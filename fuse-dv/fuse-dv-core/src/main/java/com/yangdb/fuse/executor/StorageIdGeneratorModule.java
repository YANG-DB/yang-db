package com.yangdb.fuse.executor;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.yangdb.fuse.core.driver.BasicIdGenerator;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.executor.ontology.schema.load.EntityTransformer;
import com.yangdb.fuse.model.Range;
import org.jooby.Env;

import static com.google.inject.name.Names.named;
import static com.yangdb.fuse.model.GlobalConstants.ConfigurationKeys.ID_GENERATOR_INDEX_NAME;
import static com.yangdb.fuse.model.GlobalConstants.ConfigurationKeys.ID_GENERATOR_INDEX_NAME_DEFUALT_VALUE;

public class StorageIdGeneratorModule extends ModuleBase {

    private void bindIdGenerator(Env env, Config conf, Binder binder) {
        String indexName = ID_GENERATOR_INDEX_NAME_DEFUALT_VALUE;
        try {
            indexName = conf.getString(conf.getString("assembly") + ID_GENERATOR_INDEX_NAME);
        }catch (Throwable ignore) {
            //default value is selected
        }
        binder.bindConstant().annotatedWith(named(BasicIdGenerator.indexNameParameter)).to(indexName);
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>() {}).to(BasicIdGenerator.class).asEagerSingleton();
        binder.bind(EntityTransformer.class);
    }

    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        bindIdGenerator(env, config, binder);

    }
}
