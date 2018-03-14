package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by lior.perry on 2/19/2018.
 */
public class FirstStepOnlyCostEstimator implements CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    public final static String costEstimatorParameter = "FirstStepOnlyCostEstimator.@estimator";

    //region Constructors
    @Inject
    public FirstStepOnlyCostEstimator(
            @Named(costEstimatorParameter)
            CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator) {
        this.costEstimator = costEstimator;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(Plan plan, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        return plan.getOps().size() > 2 ?
                new PlanWithCost<>(plan, context.getPreviousCost().get().getCost()) :
                this.costEstimator.estimate(plan, context);
    }
    //endregion

    //region Fields
    private CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator;
    //endregion
}
