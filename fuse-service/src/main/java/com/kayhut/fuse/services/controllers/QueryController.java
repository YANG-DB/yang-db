package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.CreateQueryAndFetchRequest;

/**
 * Created by lior on 19/02/2017.
 */
public interface QueryController {
    ContentResponse<QueryResourceInfo> create(CreateQueryRequest request);
    ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryAndFetchRequest request);
    ContentResponse<StoreResourceInfo> getInfo();
    ContentResponse<QueryResourceInfo> getInfo(String queryId);
    ContentResponse<Query> getV1(String queryId);
    ContentResponse<AsgQuery> getAsg(String queryId);
    ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId);
    ContentResponse<PlanNode<Plan>> planVerbose(String queryId);
    ContentResponse<Boolean> delete(String queryId);

}
