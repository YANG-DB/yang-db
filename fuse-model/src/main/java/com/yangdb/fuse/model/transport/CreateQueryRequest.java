package com.yangdb.fuse.model.transport;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * CreateQueryRequest.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

/**
 * Created by lior.perry on 19/02/2017.
 * <p>
 * Mutable structure due to json reflective builder needs...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateQueryRequest implements CreateQueryRequestMetadata<Query> {
    public static final String TYPE = "v1";

    //region Constructors
    public CreateQueryRequest() {
        this.planTraceOptions = new PlanTraceOptions();
        this.planTraceOptions.setLevel(PlanTraceOptions.Level.none);
        this.type = TYPE_V1_QUERY;
        this.ttl = 300000;
    }

    public CreateQueryRequest(String id, String name, Query query) {
        this();
        this.id = id;
        this.name = name;
        this.query = query;
        this.ontology = query.getOnt();
    }

    public CreateQueryRequest(String id, String name, Query query, PlanTraceOptions planTraceOptions) {
        this(id, name, query);
        this.planTraceOptions = planTraceOptions;
    }

    public CreateQueryRequest(String id, String name, Query query,  CreateCursorRequest createCursorRequest) {
        this(id, name, query, new PlanTraceOptions());
        this.createCursorRequest = createCursorRequest;
    }

    public CreateQueryRequest(String id, String name, Query query, PlanTraceOptions planTraceOptions, CreateCursorRequest createCursorRequest) {
        this(id, name, query, planTraceOptions);
        this.createCursorRequest = createCursorRequest;
    }
    //endregion

    //region Properties
    @JsonIgnore
    public CreateQueryRequest storageType(StorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    @JsonIgnore
    public CreateQueryRequest type(QueryType queryType) {
        this.queryType = queryType;
        return this;
    }

    @JsonIgnore
    public CreateQueryRequest searchPlan(boolean searchPlan) {
        this.searchPlan = searchPlan;
        return this;
    }

    @Override
    @JsonProperty("searchPlan")
    public boolean isSearchPlan() {
        return searchPlan ;
    }

    @Override
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("query")
    public void setQuery(Query query) {
        this.query = query;
    }

    @Override
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    @JsonProperty("ontology")
    public String getOntology() {
        return ontology;
    }

    @JsonProperty("ontology")
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    @Override
    @JsonProperty("query")
    public Query getQuery() {
        return query;
    }

    @JsonProperty("storageType")
    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    @JsonProperty("searchPlan")
    public void setSearchPlan(boolean searchPlan) {
        this.searchPlan = searchPlan;
    }

    @Override
    @JsonProperty("storageType")
    public StorageType getStorageType() {
        return storageType;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("planTraceOptions")
    public PlanTraceOptions getPlanTraceOptions() {
        return planTraceOptions;
    }

    @JsonProperty("planTraceOptions")
    public void setPlanTraceOptions(PlanTraceOptions planTraceOptions) {
        this.planTraceOptions = planTraceOptions;
    }

    @JsonProperty("createCursorRequest")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public CreateCursorRequest getCreateCursorRequest() {
        return createCursorRequest;
    }

    @JsonProperty("createCursorRequest")
    public void setCreateCursorRequest(CreateCursorRequest createCursorRequest) {
        this.createCursorRequest = createCursorRequest;
    }

    @JsonProperty("queryType")
    public QueryType getQueryType() {
        return queryType;
    }

    @JsonProperty("queryType")
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    @JsonProperty("ttl")
    @Override
    public long getTtl() {
        return ttl;
    }

    @JsonProperty("ttl")
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    //endregion


    @Override
    public String toString() {
        return "CreateQueryRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", query=" + QueryDescriptor.toString(query) + "\n"+
                ", createCursorRequest=" + (createCursorRequest!=null ? createCursorRequest.toString() : "None" )+
                '}';
    }

    //region Fields
    private String id;
    //default type is volatile
    private StorageType storageType = StorageType._volatile;
    private QueryType queryType = QueryType.concrete;
    private String name;
    private String type;
    private Query query;
    private String ontology;
    private long ttl;
    private boolean searchPlan = true;
    private PlanTraceOptions planTraceOptions;

    private CreateCursorRequest createCursorRequest;
    //endregion
}
