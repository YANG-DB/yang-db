package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.strategy.AsgStrategy;

/**
 * Created by User on 27/02/2017.
 */
public interface AsgStrategyRegistrar {
    Iterable<AsgStrategy> register();
}
