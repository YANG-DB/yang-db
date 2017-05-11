package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
/**
 * Created by User on 27/02/2017.
 */
public interface AsgStrategy {
    void apply(AsgQuery query, AsgStrategyContext context);
}
