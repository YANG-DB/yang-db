package com.yangdb.fuse.dispatcher.driver;

/*-
 * #%L
 * fuse-core
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
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.planTree.PlanNode;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.ExecuteStoredQueryRequest;
import com.yangdb.fuse.model.validation.ValidationResult;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by lior.perry on 21/02/2017.
 */
public interface QueryDriver {

    ValidationResult validateAndRewriteQuery(Query query);

    /**
     * create a query (V1 type) that results in a query resource
     * @param queryRequest
     * @return
     */
    Optional<QueryResourceInfo> create(CreateQueryRequest queryRequest);

    /**
     * create a query (json-type) that results in a query resource
     * @param queryRequest
     * @return
     */
    Optional<QueryResourceInfo> create(CreateJsonQueryRequest queryRequest);

    /**
     * create a prepared statement
     * type may be volatile or persistent
     * @param queryRequest
     * @return
     */
    Optional<QueryResourceInfo> call(ExecuteStoredQueryRequest queryRequest);

    /**
     * create the query resource and fetch the first page under the cursor resource
     * @param queryRequest
     * @return
     */
    @Deprecated()
    Optional<QueryResourceInfo> createAndFetch(CreateQueryRequest queryRequest);

    /**
     * run the given (V1) query by creating all the resources that are needed to execute and return the (first page) data
     * that matches the given query
     * @param query
     * @param pageSize
     * @param cursorType
     * @return
     */
    Optional<Object> run(Query query, int pageSize, String cursorType);

    /**
     * run the given (cypher) query by creating all the resources that are needed to execute and return the (first page) data
     * that matches the given query
     * @param cypher
     * @param ontology
     * @return
     */
    Optional<Object> runCypher(String cypher, String ontology);

    Optional<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType);

    /**
     * run the given (graphQL) query by creating all the resources that are needed to execute and return the (first page) data
     * that matches the given query
     * @param graphQL
     * @param ontology
     * @return
     */
    Optional<Object> runGraphQL(String graphQL, String ontology);

    /**
     * run the given (graphQL) query by creating all the resources that are needed to execute and return the (first page) data
     * that matches the given query

     * @param graphQL
     * @param ontology
     * @param pageSize
     * @param cursorType
     * @return
     */
    Optional<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType);

    /**
     * run the given (sparql) query by creating all the resources that are needed to execute and return the (first page) data
     * that matches the given query

     * @param sparql
     * @param ontology
     * @param pageSize
     * @param cursorType
     * @return
     */
    Optional<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType);

    /**
     * get the next data page according to the given query->cursor->page resource
     * @param queryId
     * @param cursorId
     * @param pageSize
     * @param deleteCurrentPage
     * @return
     */
    Optional<Object> getNextPageData(String queryId, Optional<String> cursorId,int pageSize, boolean deleteCurrentPage);

    /**
     * get general info
     * @return
     */
    Optional<StoreResourceInfo> getInfo();

    /**
     * get query resource info
     * @param queryId
     * @return
     */
    Optional<QueryResourceInfo> getInfo(String queryId);

    /**
     * get the V1 query according to the query Id
     * @param queryId
     * @return
     */
    Optional<Query> getV1(String queryId);

    /**
     *  get the ASG query tree according to the query Id
     * @param queryId
     * @return
     */
    Optional<AsgQuery> getAsg(String queryId);

    /**
     * explain the execution plan
     * @param queryId
     * @return
     */
    Optional<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId);

    /**
     * plan a logical execution plan according to the given query
     * @param query
     * @return
     */
    Optional<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query);

    /**
     * explain verbosely the execution plan
     * @param queryId
     * @return
     */
    Optional<PlanNode<Plan>> planVerbose(String queryId);

    /**
     * delete query resource and all its related resources
     * @param queryId
     * @return
     */
    Optional<Boolean> delete(String queryId);

    /**
     * show the physical traversal execution plan (tinkerpop grammer)
     * @param query
     * @return
     */
    Optional<GraphTraversal> traversal(Query query);

    Optional<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity,String targetId, String relationType, int maxHops);

    /**
     * get the vertex properties
     * @param ontology
     * @param type
     * @param vertexId
     * @return
     */
    Optional<Object> getVertex(String ontology, String type, String vertexId);

    /**
     * get the vertex neighbors
     * @param ontology
     * @param type
     * @param vertexId
     * @return
     */
    Optional<Object> getNeighbors(String ontology, String type, String vertexId);

    /**
     * profile query by execution & get profile info
     * @param queryId
     * @return
     */
    Optional<Object> profile(String queryId);
}
