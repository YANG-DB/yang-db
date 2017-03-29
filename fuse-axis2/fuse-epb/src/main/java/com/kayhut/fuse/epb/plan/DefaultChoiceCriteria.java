package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.LinkedList;

/**
 * Created by moti on 2/22/2017.
 */
public class DefaultChoiceCriteria<C> implements ChoiceCriteria<Plan<C>> {
    private Plan<C> selectedPlan = null;

    @Override
    public boolean addPlanAndCheckEndCondition(Plan<C> plan) {
        if(plan.isPlanComplete()){
            selectedPlan = plan;
        }
        return false;
    }

    @Override
    public Iterable<Plan<C>> getChosenPlans() {
        LinkedList<Plan<C>> planWrappers = new LinkedList<>();
        if(selectedPlan != null){
            planWrappers.add(selectedPlan);
        }
        return planWrappers;
    }
}
