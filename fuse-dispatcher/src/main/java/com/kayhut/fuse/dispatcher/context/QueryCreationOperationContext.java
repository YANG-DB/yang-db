package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import javaslang.Tuple2;

/**
 * Created by lior on 22/02/2017.
 */
public final class QueryCreationOperationContext extends OperationContextBase<QueryCreationOperationContext>{


    public interface Processor {
        QueryCreationOperationContext process(QueryCreationOperationContext context);
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

    public Tuple2<Plan, SingleCost> getExecutionPlan() {
        return executionPlan;
    }

    public QueryCreationOperationContext of(AsgQuery asgQuery) {
        return this.cloneImpl().asg(asgQuery);
    }

    public QueryCreationOperationContext of(Tuple2<Plan, SingleCost> executionPlan) {
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

    private QueryCreationOperationContext executionPlan(Tuple2<Plan, SingleCost> executionPlan) {
        this.executionPlan = executionPlan;
        return this;
    }
    //endregion

    //region Fields
    private QueryMetadata queryMetadata;
    private Query query;
    private AsgQuery asgQuery;
    private Tuple2<Plan, SingleCost> executionPlan;
    //endregion
}
