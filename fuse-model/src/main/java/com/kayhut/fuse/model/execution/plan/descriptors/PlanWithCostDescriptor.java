package com.kayhut.fuse.model.execution.plan.descriptors;

import com.google.inject.Inject;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class PlanWithCostDescriptor<P, C> implements Descriptor<PlanWithCost<P, C>> {
    //region Constructors
    @Inject
    public PlanWithCostDescriptor(Descriptor<? super P> planDescriptor, Descriptor<? super C> costDescriptor) {
        this.planDescriptor = planDescriptor;
        this.costDescriptor = costDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(PlanWithCost<P, C> planWithCost) {
         return "{" +
                " plan:" + this.planDescriptor.describe(planWithCost.getPlan()) + "," + "\n" +
                " cost:" + this.costDescriptor.describe(planWithCost.getCost()) + "\n" +
                 "}";
    }
    //endregion

    //region Fields
    private Descriptor<? super P> planDescriptor;
    private Descriptor<? super C> costDescriptor;
    //endregion
}