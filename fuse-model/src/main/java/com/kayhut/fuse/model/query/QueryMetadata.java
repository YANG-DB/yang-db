package com.kayhut.fuse.model.query;

/*-
 * #%L
 * QueryMetadata.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.kayhut.fuse.model.transport.CreateQueryRequest.Type;

/**
 * Created by lior.perry on 21/02/2017.
 */
public final class QueryMetadata {

    public interface QueryMetadataAble {
        QueryMetadata getQueryMetadata();
    }

    //region Properties
    public QueryMetadata(Type type,String id, String name, boolean searchPlan ,long creationTime,long ttl) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.searchPlan = searchPlan;
        this.creationTime = creationTime;
        this.ttl = ttl;
    }

    public boolean isSearchPlan() {
        return searchPlan;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getTtl() {
        return ttl;
    }
    //endregion

    //region Fields
    private long creationTime;
    private long ttl;
    private String id;
    private String name;
    private boolean searchPlan = true;
    private Type type = Type._volatile;
    //endregion
}
