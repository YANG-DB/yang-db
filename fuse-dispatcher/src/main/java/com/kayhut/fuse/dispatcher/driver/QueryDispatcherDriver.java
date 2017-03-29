package com.kayhut.fuse.dispatcher.driver;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;

import java.util.Optional;

/**
 * Created by lior on 21/02/2017.
 */
public interface QueryDispatcherDriver {
    Optional<QueryResourceInfo> create(QueryMetadata metadata, Query input);
    Optional<StoreResourceInfo> getInfo();
    Optional<QueryResourceInfo> getInfo(String queryId);
    Optional<Plan> explain(String queryId);
    Optional<Boolean> delete(String queryId);
}