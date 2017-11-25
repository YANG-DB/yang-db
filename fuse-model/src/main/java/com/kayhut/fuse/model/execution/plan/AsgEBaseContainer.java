package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by Roman on 11/25/2017.
 */
public interface AsgEBaseContainer<T extends EBase> {
    AsgEBase<T> getAsgEbase();
}
