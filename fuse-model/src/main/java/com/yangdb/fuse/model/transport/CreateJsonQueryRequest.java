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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

/**
 * Created by lior.perry on 19/02/2017.
 * <p>
 * Mutable structure due to json reflective builder needs...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateJsonQueryRequest implements CreateQueryRequestMetadata<String> {

    //region Constructors
    public CreateJsonQueryRequest(String queryType) {
        this.queryType = queryType;
        this.planTraceOptions = new PlanTraceOptions();
        this.planTraceOptions.setLevel(PlanTraceOptions.Level.none);
        this.ttl = 300000;
    }

    public CreateJsonQueryRequest(String id, String name,String queryType, String query, String ontology) {
        this(queryType);
        this.id = id;
        this.name = name;
        this.queryType = queryType;
        this.query = query;
        this.ontology = ontology;
    }

    public CreateJsonQueryRequest(String id, String name,String queryType, String query, String ontology, CreateCursorRequest createCursorRequest) {
        this(id, name, queryType,query,ontology, new PlanTraceOptions());
        this.queryType = queryType;
        this.createCursorRequest = createCursorRequest;
    }

    public CreateJsonQueryRequest(String id, String name, String queryType, String query, String ontology, PlanTraceOptions planTraceOptions) {
        this(id, name, queryType, query,ontology);
        this.planTraceOptions = planTraceOptions;
    }

    public CreateJsonQueryRequest(String id, String name,String queryType, String query, String ontology, PlanTraceOptions planTraceOptions, CreateCursorRequest createCursorRequest) {
        this(id, name,queryType, query,ontology, planTraceOptions);
        this.createCursorRequest = createCursorRequest;
    }
    //endregion

    //region Properties
    public CreateJsonQueryRequest type(StorageType type) {
        this.type = type;
        return this;
    }

    public CreateJsonQueryRequest searchPlan(boolean searchPlan) {
        this.searchPlan = searchPlan;
        return this;
    }

    public boolean isSearchPlan() {
        return searchPlan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    @Override
    public String getOntology() {
        return ontology;
    }

    public String getQuery() {
        return query;
    }

    public StorageType getStorageType() {
        return type;
    }

    @Override
    public String getType() {
        return queryType;
    }

    public PlanTraceOptions getPlanTraceOptions() {
        return planTraceOptions;
    }

    public void setPlanTraceOptions(PlanTraceOptions planTraceOptions) {
        this.planTraceOptions = planTraceOptions;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public CreateCursorRequest getCreateCursorRequest() {
        return createCursorRequest;
    }

    public void setCreateCursorRequest(CreateCursorRequest createCursorRequest) {
        this.createCursorRequest = createCursorRequest;
    }

    public QueryType getQueryType() {
        return QueryType.concrete;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    //endregion


    @Override
    public String toString() {
        return "CreateQueryRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", queryType=" + queryType + "\n"+
                ", query=" + query + "\n"+
                ", createCursorRequest=" + (createCursorRequest!=null ? createCursorRequest.toString() : "None" )+
                '}';
    }

    //region Fields
    private String id;
    //default type is volatile
    private StorageType type = StorageType._volatile;
    private String name;
    private String query;
    private String queryType;
    private String ontology;
    private long ttl;
    private boolean searchPlan = true;
    private PlanTraceOptions planTraceOptions;

    private CreateCursorRequest createCursorRequest;
    //endregion
}
