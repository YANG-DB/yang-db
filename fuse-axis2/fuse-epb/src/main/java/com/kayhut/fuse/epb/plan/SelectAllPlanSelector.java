package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.epb.plan.PlanSelector;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

/**
 * Created by benishue on 11-May-17.
 */
public class SelectAllPlanSelector<C> implements PlanSelector<PlanWithCost<Plan, C>, AsgQuery> {
    //region PlanSelector Implementation
    @Override
    public Iterable<PlanWithCost<Plan, C>> select(AsgQuery query, Iterable<PlanWithCost<Plan, C>> plans) {
        return plans;
    }
    //endregion
}
