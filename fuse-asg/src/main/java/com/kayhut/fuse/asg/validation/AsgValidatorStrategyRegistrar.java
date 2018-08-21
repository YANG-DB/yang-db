package com.kayhut.fuse.asg.validation;

/**
 * Created by User on 27/02/2017.
 */
public interface AsgValidatorStrategyRegistrar {
    Iterable<AsgValidatorStrategy> register();
}
