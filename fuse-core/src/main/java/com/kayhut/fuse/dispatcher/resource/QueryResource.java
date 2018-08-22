package com.kayhut.fuse.dispatcher.resource;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by User on 06/03/2017.
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
        this.executionPlan = executionPlan;
    }
    //endregion

    //region Public Methods
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
    private AtomicInteger cursorSequence = new AtomicInteger();
    //endregion
}
