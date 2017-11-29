package com.kayhut.fuse.dispatcher.descriptors;

import com.kayhut.fuse.model.execution.plan.composite.Plan;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class PlanDescriptor implements Descriptor<Plan> {
    //region Descriptor Implementation
    @Override
    public String describe(Plan plan) {
        return plan.toString();
    }
    //endregion
}