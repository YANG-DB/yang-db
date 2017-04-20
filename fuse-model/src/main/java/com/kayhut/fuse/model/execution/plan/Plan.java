package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.execution.plan.costs.CostEstimator;

import java.util.List;

/**
 * Created by User on 22/02/2017.
 */
public class Plan<C> {
    //region Constructors

    private Plan() {}

    public Plan(List<PlanOpWithCost<C>> ops,C cost) {
        this.ops = ops;
        this.cost = cost;
    }
    //endregion

    //region Properties
    public List<PlanOpWithCost<C>> getOps() {
        return this.ops;
    }

    public C getCost() {
        return cost;
    }

//endregion

    //region Fields
    private List<PlanOpWithCost<C>> ops;
    private C cost;

    //endregion
    public static class PlanBuilder<C> {
        private Plan<C> plan;

        private PlanBuilder(List<PlanOpWithCost<C>> ops) {
            plan = new Plan<C>();
            plan.ops = ops;
        }

        public static <C> PlanBuilder<C> build(List<PlanOpWithCost<C>> ops) {
            return new PlanBuilder<C>(ops);
        }

        public PlanBuilder<C> operation(PlanOpWithCost<C> operation) {
            plan.ops.add(operation);
            return this;
        }

        public PlanBuilder<C> cost(CostEstimator<C> cost) {
            plan.cost = cost.estimateCost(plan);
            return this;
        }

        public Plan<C> compose() {
            return plan;
        }
    }

}
