package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class PlanWithCostDescriptor implements Descriptor<PlanWithCost<Plan, PlanDetailedCost>> {
    //region Descriptor Implementation
    @Override
    public String describe(PlanWithCost planWithCost) {
        return planWithCost.toString();
    }
    //endregion
}