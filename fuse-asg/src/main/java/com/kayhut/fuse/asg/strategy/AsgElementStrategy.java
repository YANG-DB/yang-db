package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by User on 27/02/2017.
 */
public interface AsgElementStrategy<T extends EBase> {
    void apply(AsgQuery query, AsgEBase<T> element, AsgStrategyContext context);
}
