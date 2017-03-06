package com.kayhut.fuse.dispatcher;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.process.QueryResourceResult;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;

import java.util.Optional;

/**
 * Created by lior on 21/02/2017.
 */
public interface QueryDispatcherDriver {
    QueryResourceResult process(QueryMetadata metadata, Query input);
    Optional<Plan> explain(String queryId);
}
