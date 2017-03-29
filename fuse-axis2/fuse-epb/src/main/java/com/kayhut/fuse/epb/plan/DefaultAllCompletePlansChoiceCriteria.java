package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/22/2017.
 */
public class DefaultAllCompletePlansChoiceCriteria<C> implements ChoiceCriteria<Plan<C>> {
    private List<Plan<C>> selectedPlans = new LinkedList<>();


    @Override
    public boolean addPlanAndCheckEndCondition(Plan<C> plan) {
        if(plan.isPlanComplete()){
            selectedPlans.add(plan);
        }
        return false;
    }

    @Override
    public Iterable<Plan<C>> getChosenPlans() {
        return selectedPlans;
    }
}
