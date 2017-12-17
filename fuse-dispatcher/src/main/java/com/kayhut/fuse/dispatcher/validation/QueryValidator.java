package com.kayhut.fuse.dispatcher.validation;

import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.validation.QueryValidation;

/**
 * Created by Roman on 12/15/2017.
 */
public interface QueryValidator<Q> {
    QueryValidation validate(Q query);
}
