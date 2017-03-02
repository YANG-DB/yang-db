package com.kayhut.fuse.asg;

import com.kayhut.fuse.model.queryAsg.AsgQuery;

/**
 * Created by User on 27/02/2017.
 */
public interface AsgStrategy {
    void apply(AsgQuery query);
}
