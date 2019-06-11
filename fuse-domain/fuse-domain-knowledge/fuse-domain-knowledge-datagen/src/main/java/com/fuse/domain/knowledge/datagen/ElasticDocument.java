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

public class ElasticDocument<T> {
    //region Constructors
    public ElasticDocument(String index, String type, T source) {
        this(index, type, null, null, source);
    }

    public ElasticDocument(String index, String type, String id, T source) {
        this(index, type, id, null, source);
    }

    public ElasticDocument(String index, String type, String id, String routing, T source) {
        this.index = index;
        this.type = type;
        this.id = id;
        this.routing = routing;
        this.source = source;
    }
    //endregion

    //region Properties
    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getRouting() {
        return routing;
    }

    public T getSource() {
        return source;
    }
    //endregion

    //region Fields
    private String index;
    private String type;
    private String id;
    private String routing;

    private T source;
    //endregion
}
