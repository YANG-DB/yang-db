package com.kayhut.fuse.epb.plan.extenders.M1;

import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.StepAdjacentDfsStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;

/**
 * Created by Roman on 22/05/2017.
 */
public class M1DfsNonRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    public M1DfsNonRedundantPlanExtensionStrategy() {
        super(
                new CompositePlanExtensionStrategy<>(
                        new InitialPlanGeneratorExtensionStrategy(),
                        new StepAdjacentDfsStrategy()
                )
        );
    }
    //endregion
}
