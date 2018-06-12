package com.kayhut.fuse.dispatcher.resource;

import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public class QueryResource {
    //region Constructors

    public QueryResource(Query query, AsgQuery asgQuery, QueryMetadata queryMetadata, PlanWithCost<Plan, PlanDetailedCost> executionPlan, Optional<PlanNode<Plan>> planNode, String... elasticQueries) {
        this.query = query;
        this.asgQuery = asgQuery;
        this.queryMetadata = queryMetadata;
        this.planNode = planNode;
        this.cursorResources = new HashMap<>();
        this.executionPlan = executionPlan;
        this.elasticQueries = elasticQueries;
    }

    public QueryResource(Query query, AsgQuery asgQuery, QueryMetadata queryMetadata, PlanWithCost<Plan, PlanDetailedCost> executionPlan, Optional<PlanNode<Plan>> planNode) {
        this(query, asgQuery, queryMetadata, executionPlan, planNode,
                (MDC.get("requestScope" + "." + "elasticQuery") != null ? MDC.get("requestScope" + "." + "elasticQuery").split("\n") : new String[0]));
    }

    public QueryResource(Query query, AsgQuery asgQuery, QueryMetadata queryMetadata, PlanWithCost<Plan, PlanDetailedCost> executionPlan) {
        this(query, asgQuery, queryMetadata, executionPlan, Optional.empty());
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

    public void deleteCursorResource(String cursorId) {
        this.cursorResources.remove(cursorId);
    }

    public String getNextCursorId() {
        return String.valueOf(this.cursorSequence++);
    }
    //endregion

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

    public String[] getElasticQueries() {
        return elasticQueries;
    }

    public PlanWithCost<Plan, PlanDetailedCost> getExecutionPlan() {
        return this.executionPlan;
    }

    public Optional<PlanNode<Plan>> getPlanNode() {
        return planNode;
    }

    //endregion

    //region Fields
    private Query query;
    private QueryMetadata queryMetadata;
    private PlanWithCost<Plan, PlanDetailedCost> executionPlan;
    private Optional<PlanNode<Plan>> planNode;
    private AsgQuery asgQuery;
    private String[] elasticQueries;
    private Map<String, CursorResource> cursorResources;

    private int cursorSequence;
    //endregion
}
