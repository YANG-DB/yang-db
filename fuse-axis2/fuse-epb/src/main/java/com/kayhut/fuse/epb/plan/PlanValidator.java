package com.kayhut.fuse.epb.plan;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanValidator<P, Q> {
    boolean isPlanValid(P plan, Q query);
}
