package com.kayhut.fuse.dispatcher.epb;

import com.kayhut.fuse.model.validation.ValidationResult;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanValidator<P, Q>{
    ValidationResult isPlanValid(P plan, Q query);
}
