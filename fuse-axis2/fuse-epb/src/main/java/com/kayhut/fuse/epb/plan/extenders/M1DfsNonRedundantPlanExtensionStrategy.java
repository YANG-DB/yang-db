package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;

/**
 * Created by Roman on 22/05/2017.
 */
public class M1DfsNonRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    public M1DfsNonRedundantPlanExtensionStrategy() {
        super(new CompositePlanExtensionStrategy<>(new StepAdjacentDfsStrategy()));
    }
    //endregion
}
