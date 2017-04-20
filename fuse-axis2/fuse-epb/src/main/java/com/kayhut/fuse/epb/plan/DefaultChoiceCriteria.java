package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.LinkedList;

/**
 * Created by moti on 2/22/2017.
 */
public class DefaultChoiceCriteria<C> implements ChoiceCriteria<PlanWithCost<Plan, C>, AsgQuery> {
    private PlanWithCost<Plan, C> selectedPlan = null;

    @Override
    public boolean addPlanAndCheckEndCondition(AsgQuery query, PlanWithCost<Plan, C> planWithCost) {
        if(SimpleExtenderUtils.checkIfPlanIsComplete(planWithCost.getPlan(), query)){
            selectedPlan = planWithCost;
        }
        return false;
    }

    @Override
    public Iterable<PlanWithCost<Plan, C>> getChosenPlans() {
        LinkedList<PlanWithCost<Plan, C>> planWrappers = new LinkedList<>();
        if(selectedPlan != null){
            planWrappers.add(selectedPlan);
        }
        return planWrappers;
    }
}
