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

    Optional<QueryResourceInfo> create(CreateQueryRequest queryRequest);

    Optional<QueryResourceInfo> create(CreateJsonQueryRequest queryRequest);

    Optional<QueryResourceInfo> call(ExecuteStoredQueryRequest queryRequest);

    Optional<QueryResourceInfo> createAndFetch(CreateQueryRequest queryRequest);

    Optional<Object> run(Query query, int pageSize, String cursorType);

    Optional<Object> runCypher(String cypher, String ontology);

    Optional<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType);

    Optional<Object> runGraphQL(String graphQL, String ontology);

    Optional<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType);

    Optional<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType);

    Optional<Object> getNextPageData(String queryId, Optional<String> cursorId,int pageSize, boolean deleteCurrentPage);

    Optional<StoreResourceInfo> getInfo();

    Optional<QueryResourceInfo> getInfo(String queryId);

    Optional<Query> getV1(String queryId);

    Optional<AsgQuery> getAsg(String queryId);

    Optional<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId);

    Optional<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query);

    Optional<PlanNode<Plan>> planVerbose(String queryId);

    Optional<Boolean> delete(String queryId);

    Optional<GraphTraversal> traversal(Query query);

    Optional<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity,String targetId, String relationType, int maxHops);

    Optional<Object> getVertex(String ontology, String type, String vertexId);

    Optional<Object> getNeighbors(String ontology, String type, String vertexId);


}
