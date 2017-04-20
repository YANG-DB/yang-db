package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/21/2017.
 */
public interface ChoiceCriteria<P,Q> {
    boolean addPlanAndCheckEndCondition(Q query, P plan);
    Iterable<P> getChosenPlans();
}
