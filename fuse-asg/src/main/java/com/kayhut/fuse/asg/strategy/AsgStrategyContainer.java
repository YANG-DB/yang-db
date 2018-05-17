package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;

import java.util.Arrays;

public class AsgStrategyContainer implements AsgStrategy {

    public AsgStrategyContainer(AsgStrategy ... strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Arrays.asList(strategies).forEach(p->p.apply(query,context));
    }

    private AsgStrategy[] strategies;
}
