package com.yangdb.fuse.dispatcher.resource;

/*-
 * #%L
 * fuse-core
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



import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.planTree.PlanNode;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.transport.CreateQueryRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lior.perry on 06/03/2017.
 */
public class QueryResource {
    //region Constructors
    public QueryResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata queryMetadata, PlanWithCost<Plan, PlanDetailedCost> executionPlan) {
        this(request, query, asgQuery, queryMetadata, executionPlan, Optional.empty());
    }

    public QueryResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata queryMetadata, PlanWithCost<Plan, PlanDetailedCost> executionPlan, Optional<PlanNode<Plan>> planNode) {
        this.request = request;
        this.query = query;
        this.asgQuery = asgQuery;
        this.queryMetadata = queryMetadata;
        this.planNode = planNode;
        this.cursorResources = new HashMap<>();
        this.innerQueryResources = new HashMap<>();
        this.executionPlan = executionPlan;
    }
    //endregion

    //region Public Methods
    public QueryResource withInnerQueryResources(List<QueryResource> resources) {
        resources.forEach(this::addInnerQueryResource);
        return this;
    }

    public void addInnerQueryResource(QueryResource resource) {
        this.innerQueryResources.put(resource.getQueryMetadata().getId(), resource);
    }

    public Iterable<QueryResource> getInnerQueryResources() {
        return this.innerQueryResources.values();
    }

    public void addCursorResource(String cursorId, CursorResource cursorResource) {
        this.cursorResources.put(cursorId, cursorResource);
    }

    public Iterable<CursorResource> getCursorResources() {
        return this.cursorResources.values();
    }

    public Optional<CursorResource> getCursorResource(String cursorId) {
        return Optional.ofNullable(this.cursorResources.get(cursorId));
    }

    //endregion

    public void deleteCursorResource(String cursorId) {
        this.cursorResources.remove(cursorId);
    }

    public String getNextCursorId() {
        return String.valueOf(this.cursorSequence.incrementAndGet());
    }

    public String getCurrentCursorId() {
        return String.valueOf(this.cursorSequence.get());
    }

    //region Properties
    public Query getQuery() {
        return this.query;
    }

    public AsgQuery getAsgQuery() {
        return this.asgQuery;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public PlanWithCost<Plan, PlanDetailedCost> getExecutionPlan() {
        return this.executionPlan;
    }

    public CreateQueryRequest getRequest() {
        return request;
    }

    public Optional<PlanNode<Plan>> getPlanNode() {
        return planNode;
    }
    //endregion

    //region Fields
    private Query query;
    private CreateQueryRequest request;
    private QueryMetadata queryMetadata;
    private PlanWithCost<Plan, PlanDetailedCost> executionPlan;
    private Optional<PlanNode<Plan>> planNode;
    private AsgQuery asgQuery;
    private Map<String, CursorResource> cursorResources;
    private Map<String, QueryResource> innerQueryResources;
    private AtomicInteger cursorSequence = new AtomicInteger();
    //endregion
}
