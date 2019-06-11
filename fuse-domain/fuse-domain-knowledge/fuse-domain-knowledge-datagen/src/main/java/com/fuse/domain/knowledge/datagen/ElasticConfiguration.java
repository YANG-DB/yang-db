package com.fuse.domain.knowledge.datagen;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

/**
 * Created by Roman on 6/22/2018.
 */
public class ElasticConfiguration {
    //region Construtors
    public ElasticConfiguration() {}

    public ElasticConfiguration(Iterable<String> hosts, LightSchema readSchema, LightSchema writeSchema) {
        this.hosts = hosts;
        this.readSchema = readSchema;
        this.writeSchema = writeSchema;
    }
    //endregion

    //region Properties
    public Iterable<String> getHosts() {
        return hosts;
    }

    public void setHosts(Iterable<String> hosts) {
        this.hosts = hosts;
    }

    public LightSchema getReadSchema() {
        return readSchema;
    }

    public void setReadSchema(LightSchema readSchema) {
        this.readSchema = readSchema;
    }

    public LightSchema getWriteSchema() {
        return writeSchema;
    }

    public void setWriteSchema(LightSchema writeSchema) {
        this.writeSchema = writeSchema;
    }
    //endregion

    //region Fields
    private Iterable<String> hosts;
    private LightSchema readSchema;
    private LightSchema writeSchema;
    //endregion
}
