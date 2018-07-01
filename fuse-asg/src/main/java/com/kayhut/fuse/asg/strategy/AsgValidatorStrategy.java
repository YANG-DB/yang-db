package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.validation.ValidationResult;

/**
 * Created by User on 27/02/2017.
 */
public interface AsgValidatorStrategy {
    ValidationResult apply(AsgQuery query, AsgStrategyContext context);
}
