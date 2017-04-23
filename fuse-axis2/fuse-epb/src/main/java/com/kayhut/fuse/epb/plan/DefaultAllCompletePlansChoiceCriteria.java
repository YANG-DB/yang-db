package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/22/2017.
 */
public class DefaultAllCompletePlansChoiceCriteria<C> implements ChoiceCriteria<PlanWithCost<Plan, C>, AsgQuery> {
    private List<PlanWithCost<Plan, C>> selectedPlans = new LinkedList<>();

    @Override
    public boolean addPlanAndCheckEndCondition(AsgQuery query, PlanWithCost<Plan, C> planWithCost) {
        if(SimpleExtenderUtils.checkIfPlanIsComplete(planWithCost.getPlan(), query)){
            selectedPlans.add(planWithCost);
        }
        return false;
    }

    @Override
    public Iterable<PlanWithCost<Plan, C>> getChosenPlans() {
        return selectedPlans;
    }
}
