package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.query.properties.EProp;

public interface RuleBoostProvider {

    long getBoost(EProp eProp, int ruleIndex);

}
