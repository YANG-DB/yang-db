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
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

/**
 * Created by lior.perry on 19/02/2017.
 * <p>
 * Mutable structure due to json reflective builder needs...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateJsonQueryRequest implements CreateQueryRequestMetadata<String> {
    public CreateJsonQueryRequest() {
        this.planTraceOptions = new PlanTraceOptions();
        this.planTraceOptions.setLevel(PlanTraceOptions.Level.none);
        this.ttl = 300000;
        this.queryType = QueryType.concrete;
    }

    //region Constructors
    public CreateJsonQueryRequest(String type) {
        this();
        this.type = type;
    }

    public CreateJsonQueryRequest(String id, String name,String type, String query, String ontology) {
        this(type);
        this.id = id;
        this.name = name;
        this.type = type;
        this.query = query;
        this.ontology = ontology;
    }

    public CreateJsonQueryRequest(String id, String name,String type, String query, String ontology, CreateCursorRequest createCursorRequest) {
        this(id, name, type,query,ontology, new PlanTraceOptions());
        this.type = type;
        this.createCursorRequest = createCursorRequest;
    }

    public CreateJsonQueryRequest(String id, String name, String type, String query, String ontology, PlanTraceOptions planTraceOptions) {
        this(id, name, type, query,ontology);
        this.planTraceOptions = planTraceOptions;
    }

    public CreateJsonQueryRequest(String id, String name,String type, String query, String ontology, PlanTraceOptions planTraceOptions, CreateCursorRequest createCursorRequest) {
        this(id, name,type, query,ontology, planTraceOptions);
        this.createCursorRequest = createCursorRequest;
    }
    //endregion

    //region Properties
    @JsonIgnore
    public CreateJsonQueryRequest storeType(StorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    @JsonIgnore
    public CreateJsonQueryRequest searchPlan(boolean searchPlan) {
        this.searchPlan = searchPlan;
        return this;
    }

    @JsonProperty("searchPlan")
    public boolean isSearchPlan() {
        return searchPlan;
    }

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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("query")
    public void setQuery(String query) {
        this.query = query;
    }

    @JsonProperty("ontology")
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    @JsonProperty("storageType")
    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("searchPlan")
    public void setSearchPlan(boolean searchPlan) {
        this.searchPlan = searchPlan;
    }

    @Override
    @JsonProperty("ontology")
    public String getOntology() {
        return ontology;
    }

    @JsonProperty("query")
    public String getQuery() {
        return query;
    }

    @JsonProperty("storageType")
    public StorageType getStorageType() {
        return storageType;
    }

    @Override
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("createCursorRequest")
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

    @JsonProperty("ttl")
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
                ", queryType=" + type + "\n"+
                ", query=" + query + "\n"+
                ", createCursorRequest=" + (createCursorRequest!=null ? createCursorRequest.toString() : "None" )+
                '}';
    }

    //region Fields
    private String id;
    //default type is volatile
    private StorageType storageType = StorageType._volatile;
    private String name;
    private String query;
    private QueryType queryType;
    private String type;
    private String ontology;
    private long ttl;
    private boolean searchPlan = true;
    private PlanTraceOptions planTraceOptions;

    private CreateCursorRequest createCursorRequest;
    //endregion
}
