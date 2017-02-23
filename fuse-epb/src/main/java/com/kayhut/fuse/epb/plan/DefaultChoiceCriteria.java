package com.kayhut.fuse.epb.plan;

import java.util.LinkedList;

/**
 * Created by moti on 2/22/2017.
 */
public class DefaultChoiceCriteria<P, C, Q> implements ChoiceCriteria<P, C, Q> {
    private PlanWrapper<P, C> selectedPlan = null;

    @Override
    public boolean addPlanAndCheckEndCondition(PlanWrapper<P, C> plan, Q query) {
        if(plan.isPlanComplete()){
            selectedPlan = plan;
        }
        return false;
    }

    @Override
    public Iterable<PlanWrapper<P, C>> getChosenPlans() {
        LinkedList<PlanWrapper<P, C>> planWrappers = new LinkedList<>();
        if(selectedPlan != null){
            planWrappers.add(selectedPlan);
        }
        return planWrappers;
    }
}
