package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.DummyStrategy;

import java.util.Arrays;

/**
 * Created by User on 05/03/2017.
 */
public class DefaultStrategyRegistrar implements AsgStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new DummyStrategy()
        );
    }
    //endregion
}
