package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.ExecuteStoredQueryRequest;

/**
 * Created by lior on 19/02/2017.
 */
public interface QueryController {
    /**
     * create a prepared statement
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> create(CreateQueryRequest request);

    /**
     * create a prepared statement, run against db and return results
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request);

    /**
     * call existing statement and, run against db and return results
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request);

    /**
     * get queries info
     * @return
     */
    ContentResponse<StoreResourceInfo> getInfo();

    /**
     * get specific query info
     * @param queryId
     * @return
     */
    ContentResponse<QueryResourceInfo> getInfo(String queryId);

    /**
     * get v1 query for a given query
     * @param queryId
     * @return
     */
    ContentResponse<Query> getV1(String queryId);
    /**
     * get asg query for a given query
     * @param queryId
     * @return
     */
    ContentResponse<AsgQuery> getAsg(String queryId);
    /**
     * explain execution plan for a given query
     * @param queryId
     * @return
     */
    ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId);

    ContentResponse<PlanNode<Plan>> planVerbose(String queryId);

    /**
     * delete query resource
     * @param queryId
     * @return
     */
    ContentResponse<Boolean> delete(String queryId);

}
