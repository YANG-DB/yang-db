package com.kayhut.fuse.epb.plan;

import java.util.Comparator;

/**
 * Created by moti on 2/21/2017.
 */
public interface ChoiceCriteria<P,C> {
    boolean addPlanAndCheckEndCondition(PlanWrapper<P,C> plan);
    Iterable<PlanWrapper<P,C>> getChosenPlans();
}
