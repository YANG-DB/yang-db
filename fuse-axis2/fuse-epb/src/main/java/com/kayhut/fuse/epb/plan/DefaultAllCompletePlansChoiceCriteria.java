package com.kayhut.fuse.epb.plan;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/22/2017.
 */
public class DefaultAllCompletePlansChoiceCriteria<P, C> implements ChoiceCriteria<P, C> {
    private List<PlanWrapper<P, C>> selectedPlans = new LinkedList<>();


    @Override
    public boolean addPlanAndCheckEndCondition(PlanWrapper<P, C> plan) {
        if(plan.isPlanComplete()){
            selectedPlans.add(plan);
        }
        return false;
    }

    @Override
    public Iterable<PlanWrapper<P, C>> getChosenPlans() {
        return selectedPlans;
    }
}
