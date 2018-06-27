package com.kayhut.fuse.dispatcher.validation;

import com.kayhut.fuse.model.validation.ValidationResult;

/**
 * Created by Roman on 12/15/2017.
 */
public interface QueryValidator<Q> {
    ValidationResult validate(Q query);
}
