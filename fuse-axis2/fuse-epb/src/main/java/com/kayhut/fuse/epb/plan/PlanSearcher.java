package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanSearcher<P,Q> {
     Iterable<P> build(Q query, ChoiceCriteria<P,Q> choiceCriteria);
}
