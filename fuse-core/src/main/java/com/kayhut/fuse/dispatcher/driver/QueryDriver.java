package com.kayhut.fuse.dispatcher.driver;

import com.fasterxml.jackson.databind.JsonNode;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;

import java.util.Optional;

/**
 * Created by lior on 21/02/2017.
 */
public interface QueryDriver {
    Optional<QueryResourceInfo> create(QueryMetadata metadata, Query input);

    Optional<StoreResourceInfo> getInfo();

    Optional<QueryResourceInfo> getInfo(String queryId);

    Optional<Query> getV1(String queryId);

    Optional<AsgQuery> getAsg(String queryId);

    Optional<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId);

    Optional<PlanNode<Plan>> planVerbose(String queryId);

    Optional<Boolean> delete(String queryId);
}
