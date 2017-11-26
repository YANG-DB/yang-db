package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by moti on 2/23/2017.
 */
public class DummyValidator<P,Q> implements PlanValidator<P,Q> {
    @Override
    public ValidationContext isPlanValid(P plan, Q query) {
        return ValidationContext.OK;
    }
}
