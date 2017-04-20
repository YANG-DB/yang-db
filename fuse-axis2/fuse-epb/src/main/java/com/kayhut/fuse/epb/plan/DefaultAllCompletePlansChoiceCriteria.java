package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/22/2017.
 */
public class DefaultAllCompletePlansChoiceCriteria<C> implements ChoiceCriteria<Plan<C>,AsgQuery> {
    private List<Plan<C>> selectedPlans = new LinkedList<>();


    @Override
    public boolean addPlanAndCheckEndCondition(AsgQuery query, Plan<C> plan) {
        if(SimpleExtenderUtils.checkIfPlanIsComplete(plan, query)){
            selectedPlans.add(plan);
        }
        return false;
    }

    @Override
    public Iterable<Plan<C>> getChosenPlans() {
        return selectedPlans;
    }
}
