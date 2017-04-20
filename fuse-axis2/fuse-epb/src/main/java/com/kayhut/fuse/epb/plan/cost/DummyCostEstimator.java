package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Optional;

/**
 * Created by moti on 3/28/2017.
 */
public class DummyCostEstimator<P, C> implements CostEstimator<P, C> {
    public DummyCostEstimator(C dummyCost) {
        this.dummyCost = dummyCost;
    }

    @Override
    public PlanWithCost<P, C> estimate(P plan, Optional<C> previousCost) {
        return new PlanWithCost<>(plan, previousCost.isPresent() ? previousCost.get() : dummyCost);
    }

    private C dummyCost;
}
