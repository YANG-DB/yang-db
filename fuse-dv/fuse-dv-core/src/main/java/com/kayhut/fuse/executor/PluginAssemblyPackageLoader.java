package com.kayhut.fuse.executor;

/*-
 * #%L
 * fuse-dv-core
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

import com.kayhut.fuse.executor.ontology.schema.GraphDataLoader;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.typesafe.config.Config;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by lior.perry on 2/18/2018.
 */
public class PluginAssemblyPackageLoader extends ExecutorModule {

    protected Class<? extends RawSchema> getRawElasticSchema(Config conf) throws ClassNotFoundException {
        return (Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly")+".physical_raw_schema"));
    }

    protected GraphDataLoader getInitialDataLoader(Config conf) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        RawSchema rawSchema = getRawElasticSchema(conf).newInstance();
        GraphDataLoader initialGraphDataLoader =
                (GraphDataLoader) (Class.forName(
                        conf.getString(conf.getString("assembly")+".physical_schema_data_loader"))
                        .getConstructor(Config.class, RawSchema.class)
                        .newInstance(conf, rawSchema));

        return initialGraphDataLoader;
    }


}
