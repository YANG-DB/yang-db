package com.kayhut.fuse.asg;

import com.google.common.eventbus.Subscribe;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.queryAsg.AsgQuery;

/**
 * Created by benishue on 27-Feb-17.
 */
public interface Asgbuilder {
    @Subscribe
    AsgQuery transformQueryToAsgQuery(Query query);
}
