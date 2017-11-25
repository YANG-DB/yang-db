package com.kayhut.fuse.dispatcher.context;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.descriptor.Descriptor;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

import java.util.Optional;

/**
 * Created by lior on 22/02/2017.
 */
public final class QueryCreationOperationContext extends OperationContextBase<QueryCreationOperationContext> implements QueryMetadata.QueryMetadataAble {


    public interface Processor {
        QueryCreationOperationContext process(QueryCreationOperationContext context) throws Exception;
    }

    //region constructors
    public QueryCreationOperationContext(QueryMetadata queryMetadata, Query query) {
        this(queryMetadata,query,Optional.empty());
    }

    public QueryCreationOperationContext(QueryMetadata queryMetadata, Query query,Optional<PlanNode<Plan>> planNode ) {
        this.queryMetadata = queryMetadata;
        this.query = query;
        this.planNode = planNode;
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

    public Optional<PlanNode<Plan>> getPlanNode() {
        return planNode;
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

    public QueryCreationOperationContext of(Optional<PlanNode<Plan>> planNode) {
        return this.cloneImpl().planNode(planNode);
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
        clone.planNode = this.planNode;
        return clone;
    }

    private QueryCreationOperationContext planNode(Optional<PlanNode<Plan>> planNode) {
        this.planNode = planNode;
        return this;
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
    private Optional<PlanNode<Plan>> planNode;
    //endregion

    public static class QueryCreationOperationContextDescriptor implements Descriptor<QueryCreationOperationContext>{

        @Override
        public String name(QueryCreationOperationContext query) {
            return query.getQuery().getName();
        }

        @Override
        public String describe(QueryCreationOperationContext query) {
            String asg = "null";
            if(query.getAsgQuery() != null)
                asg = AsgQueryUtil.patternValue(query.getAsgQuery());
            return "{Query: {Name:" + query.getQuery().getName() + ",Ont:" + query.getQuery().getOnt()+"}," +
                    "AsgQuery: {" +asg + "}}" ;
        }
    }
}
