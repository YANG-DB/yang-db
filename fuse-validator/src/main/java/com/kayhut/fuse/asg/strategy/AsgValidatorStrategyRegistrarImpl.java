package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.strategy.validation.AsgEntityPropertiesValidationValidatorStrategy;

import java.util.Arrays;

public class AsgValidatorStrategyRegistrarImpl implements AsgValidatorStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgValidatorStrategy> register() {
        return Arrays.asList(
                new AsgEntityPropertiesValidationValidatorStrategy()
        );
    }
    //endregion
}
