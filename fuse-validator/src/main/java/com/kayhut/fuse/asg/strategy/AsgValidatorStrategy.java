package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;

/**
 * Created by User on 27/02/2017.
 */
public interface AsgValidatorStrategy {
    ValidationResult apply(AsgQuery query, AsgStrategyContext context);
}
