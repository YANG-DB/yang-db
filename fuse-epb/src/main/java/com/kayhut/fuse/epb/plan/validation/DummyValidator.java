package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;

/**
 * Created by moti on 2/23/2017.
 */
public class DummyValidator<P,Q> implements PlanValidator<P,Q> {
    @Override
    public boolean isPlanValid(P plan, Q query) {
        return true;
    }
}
