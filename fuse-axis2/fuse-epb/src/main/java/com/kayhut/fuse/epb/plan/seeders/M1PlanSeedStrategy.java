package com.kayhut.fuse.epb.plan.seeders;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;

/**
 * Created by Roman on 04/07/2017.
 */
public class M1PlanSeedStrategy extends CompositePlanSeedStrategy<Plan, AsgQuery> {
    //region Constructors
    public M1PlanSeedStrategy() {
        super(new InitialPlanGeneratorSeedStrategy());
    }
    //endregion
}
