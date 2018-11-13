package com.kayhut.fuse.generator.configuration;

/*-
 * #%L
 * fuse-domain-gragons-datagen
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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by benishue on 15-May-17.
 */
public class DataGenConfiguration {

    private Configuration configuration;

    public DataGenConfiguration(String configPath) {
        configuration = setConfiguration(configPath);
    }

    private synchronized Configuration setConfiguration(String configPath) {
        try {
            if (configuration != null) {
                return configuration;
            }
            configuration = new PropertiesConfiguration(configPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return configuration;
    }

    public Configuration getInstance()
    {
        return configuration;
    }
}
