package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.log.Trace;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanValidator<P, Q> extends Trace<String> {
    boolean isPlanValid(P plan, Q query);
}
