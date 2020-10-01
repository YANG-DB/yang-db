package com.yangdb.fuse.services;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.google.inject.Inject;
import com.yangdb.fuse.ext.driver.ExtensionQueryDriver;
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
import com.yangdb.fuse.services.controllers.QueryController;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

public class KnowledgeExtensionQueryController implements QueryController<QueryController,ExtensionQueryDriver> {

    private final ExtensionQueryDriver driver;
    private final QueryController controller;

    //region Constructors
    @Inject
    public KnowledgeExtensionQueryController(
            ExtensionQueryDriver driver,
            QueryController controller) {

        this.driver = driver;
        this.controller = (QueryController) controller.driver(driver);
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        return controller.create(request);
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        return controller.create(request);
    }

    @Override
    public ContentResponse<Object> runV1Query(Query query, int pageSize, String cursorType) {
        return controller.runV1Query(query,pageSize,cursorType );
    }

    @Override
    public ContentResponse<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType) {
        return controller.runGraphQL(graphQL, ontology, pageSize, cursorType);
    }

    @Override
    public ContentResponse<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType) {
        return controller.runSparql(sparql, ontology, pageSize, cursorType);
    }

    @Override
    public ContentResponse<ValidationResult> validate(Query query) {
        return controller.validate(query);
    }

    @Override
    public ContentResponse<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity, String targetId, String relationType, int maxHops) {
        return controller.findPath(ontology,sourceEntity, sourceId, targetEntity, targetId, relationType, maxHops);
    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology) {
        return controller.runCypher(cypher,ontology);
    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType) {
        return controller.runCypher(cypher,ontology,pageSize,cursorType);
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return controller.createAndFetch(request);
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request) {
        return controller.createAndFetch(request);
    }

    @Override
    public ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request) {
       return controller.callAndFetch(request);
    }

    @Override
    public ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        return controller.fetchNextPage(queryId,cursorId,pageSize,deleteCurrentPage);
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return controller.getInfo();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return controller.getInfo(queryId);
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        return controller.getV1(queryId);
    }

    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        return controller.getAsg(queryId);
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return controller.explain(queryId);
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return controller.planVerbose(queryId);
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return controller.delete(queryId);
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
        return controller.plan(query);
    }

    @Override
    public ContentResponse<GraphTraversal> traversal(Query query) {
        return controller.traversal(query);
    }

    @Override
    public QueryController driver(ExtensionQueryDriver driver) {
        return this;
    }

    //endregion

}
