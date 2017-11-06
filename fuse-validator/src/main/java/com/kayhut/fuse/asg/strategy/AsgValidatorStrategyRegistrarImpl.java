package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.strategy.validation.*;

import java.util.Arrays;
import java.util.Collections;

public class AsgValidatorStrategyRegistrarImpl implements AsgValidatorStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgValidatorStrategy> register() {
        return Collections.singletonList(new CompositeValidatorStrategy(
                new AsgCycleValidatorStrategy(),
                new AsgEntityPropertiesValidatorStrategy(),
                new AsgOntologyEntityValidatorStrategy(),
                new AsgOntologyRelValidatorStrategy(),
                new AsgRelPropertiesValidatorStrategy(),
                new AsgStartEntityValidatorStrategy(),
                new AsgStepsValidatorStrategy()
        ));
    }
    //endregion
}
