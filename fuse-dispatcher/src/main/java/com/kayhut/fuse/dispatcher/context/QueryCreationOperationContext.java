package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

/**
 * Created by lior on 22/02/2017.
 */
public final class QueryCreationOperationContext extends OperationContextBase<QueryCreationOperationContext> implements QueryMetadata.QueryMetadataAble {


    public interface Processor {
        QueryCreationOperationContext process(QueryCreationOperationContext context) throws Exception;
    }

    //region constructors
    public QueryCreationOperationContext(QueryMetadata queryMetadata, Query query) {
        this.queryMetadata = queryMetadata;
        this.query = query;
    }
    //endregion

    //region Public Methods
    @Override
    protected QueryCreationOperationContext clone() throws CloneNotSupportedException {
        return (QueryCreationOperationContext)super.clone();
    }

    //endregion

    //region properties
    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public Query getQuery() {
        return query;
    }

    public AsgQuery getAsgQuery() {
        return asgQuery;
    }

    public PlanWithCost<Plan, PlanDetailedCost> getExecutionPlan() {
        return executionPlan;
    }

    public QueryCreationOperationContext of(AsgQuery asgQuery) {
        return this.cloneImpl().asg(asgQuery);
    }

    public QueryCreationOperationContext of(PlanWithCost<Plan, PlanDetailedCost> executionPlan) {
        return this.cloneImpl().executionPlan(executionPlan);
    }
    //endregion

    //region Private Methods
    @Override
    protected QueryCreationOperationContext cloneImpl() {
        QueryCreationOperationContext clone = new QueryCreationOperationContext(this.queryMetadata, this.query);
        clone.asgQuery = this.asgQuery;
        clone.executionPlan = this.executionPlan;
        return clone;
    }

    private QueryCreationOperationContext asg(AsgQuery asgQuery) {
        this.asgQuery = asgQuery;
        return this;
    }

    private QueryCreationOperationContext executionPlan(PlanWithCost<Plan, PlanDetailedCost> executionPlan) {
        this.executionPlan = executionPlan;
        return this;
    }
    //endregion

    //region Fields
    private QueryMetadata queryMetadata;
    private Query query;
    private AsgQuery asgQuery;
    private PlanWithCost<Plan, PlanDetailedCost> executionPlan;
    //endregion
}
