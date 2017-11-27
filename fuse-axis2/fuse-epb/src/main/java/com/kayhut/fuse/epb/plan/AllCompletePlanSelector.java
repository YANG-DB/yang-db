package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moti on 2/22/2017.
 */
public class AllCompletePlanSelector<C> implements PlanSelector<PlanWithCost<Plan, C>, AsgQuery> {
    //region PlanSelector Implementation
    @Override
    public Iterable<PlanWithCost<Plan, C>> select(AsgQuery query, Iterable<PlanWithCost<Plan, C>> plans) {
        List<PlanWithCost<Plan, C>> selectedPlans = new ArrayList<>();
        for(PlanWithCost<Plan, C> planWithCost : plans) {
            if(SimpleExtenderUtils.checkIfPlanIsComplete(planWithCost.getPlan(), query)){
                selectedPlans.add(planWithCost);
            }
        }

        return selectedPlans;
    }
    //endregion
}
