package com.kayhut.fuse.asg.strategy;

/**
 * Created by User on 27/02/2017.
 */
public interface AsgStrategyRegistrar {
    Iterable<AsgStrategy> register();
}
