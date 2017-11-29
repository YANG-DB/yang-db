package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.dispatcher.epb.PlanValidator;

/**
 * Created by moti on 2/23/2017.
 */
public class DummyValidator<P,Q> implements PlanValidator<P,Q> {
    @Override
    public ValidationContext isPlanValid(P plan, Q query) {
        return ValidationContext.OK;
    }
}
