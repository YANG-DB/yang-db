package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.planTree.PlanNode;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.ExecuteStoredQueryRequest;
import com.yangdb.fuse.model.validation.ValidationResult;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface QueryController<C,D> extends Controller<C,D> {
    /**
     * create a prepared statement
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> create(CreateQueryRequest request);
    /**
     * create a prepared statement
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request);

    /**
     * run a stateless query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param `query
     * @param pageSize
     * @param cursorType
     * @return
     */
    ContentResponse<Object> run(Query query, int pageSize, String cursorType);

    /**
     * run a stateless query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param query
     * @return
     */
    ContentResponse<ValidationResult> validate(Query query);

    /**
     * run a stateless cypher query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param cypher
     * @param ontology
     * @return
     */
    ContentResponse<Object> run(String cypher, String ontology);


    /**
     * run a stateless cypher query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param `query
     * @param ontology
     * @param pageSize
     * @param cursorType
     * @return
     */
    ContentResponse<Object> run(String cypher, String ontology, int pageSize, String cursorType);


    /**
     * create a prepared statement, run against db and return results
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request);

    /**
     * create a prepared statement, run against db and return results
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request);

    /**
     * call existing statement and, run against db and return results
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request);


    /**
     *
     * @param queryId
     * @param cursorId
     * @param pageSize
     * @param deleteCurrentPage
     * @return
     */
    ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage);

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

    /**
     * get the plan generated by the v1 query
     * @param query
     * @return
     */
    ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query);

    /**
     * get the traversal execution instruction - gremlin
     * @param query
     * @return
     */
    ContentResponse<GraphTraversal> traversal(Query query);
}
