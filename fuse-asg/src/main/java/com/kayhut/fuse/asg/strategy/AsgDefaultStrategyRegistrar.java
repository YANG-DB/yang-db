package com.kayhut.fuse.asg.strategy;

import java.util.Arrays;

/**
 * Created by User on 05/03/2017.
 */
public class AsgDefaultStrategyRegistrar implements AsgStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new AsgDummyStrategy()
        );
    }
    //endregion
}
