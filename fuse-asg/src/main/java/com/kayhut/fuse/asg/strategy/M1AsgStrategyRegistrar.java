package com.kayhut.fuse.asg.strategy;

import java.util.Arrays;

/**
 * Created by Roman on 5/8/2017.
 */
public class M1AsgStrategyRegistrar implements AsgStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new AsgEntityPropertiesGroupingStrategy(),
                new AsgHQuantifierPropertiesGroupingStrategy(),
                new AsgQuant1PropertiesGroupingStrategy(),
                new AsgRelPropertiesGroupingStrategy()
        );
    }
    //endregion
}
