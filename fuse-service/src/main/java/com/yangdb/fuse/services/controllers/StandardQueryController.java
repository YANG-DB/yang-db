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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.driver.QueryDriver;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.planTree.PlanNode;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.*;
import com.yangdb.fuse.model.transport.ContentResponse.Builder;
import com.yangdb.fuse.model.validation.ValidationResult;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Collections;
import java.util.Optional;

import static org.jooby.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardQueryController implements QueryController<QueryController,QueryDriver> {
    public static final String cursorControllerParameter = "StandardQueryController.@cursorController";
    public static final String pageControllerParameter = "StandardQueryController.@pageController";

    //region Constructors
    @Inject
    public StandardQueryController(
            QueryDriver driver,
            @Named(cursorControllerParameter) CursorController cursorController,
            @Named(pageControllerParameter) PageController pageController) {
        this.driver = driver;
        this.cursorController = cursorController;
        this.pageController = pageController;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR )
                .data(driver().create(request))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }

    protected QueryDriver driver() {
        return driver;
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR )
                .data(driver().create(request))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }

    @Override
    public ContentResponse<Object> runV1Query(Query query, int pageSize, String cursorType) {
        return Builder.builder(CREATED, SERVER_ERROR )
                .data(driver().run(query,pageSize,cursorType ))
                .successPredicate(objectContentResponse -> true)
                .compose();

    }

    @Override
    public ContentResponse<ValidationResult> validate(Query query) {
        return Builder.<ValidationResult>builder(CREATED, SERVER_ERROR )
                .data(Optional.of(driver().validateAndRewriteQuery(query)))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity,String targetId, String relationType, int maxHops) {
        return Builder.builder(CREATED, SERVER_ERROR )
                .data(Optional.of(driver().findPath(ontology,sourceEntity,sourceId,targetEntity,targetId,relationType,maxHops)))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology) {
        return Builder.builder(CREATED, SERVER_ERROR )
                .data(driver().runCypher(cypher,ontology))
                .successPredicate(objectContentResponse -> true)
                .compose();

    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType) {
        return Builder.builder(CREATED, SERVER_ERROR )
                .data(driver().runCypher(cypher,ontology,pageSize,cursorType))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType) {
        return Builder.builder(CREATED, SERVER_ERROR )
                .data(driver().runGraphQL(graphQL,ontology,pageSize,cursorType))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }
    @Override
    public ContentResponse<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType) {
        return Builder.builder(CREATED, SERVER_ERROR )
                .data(driver().runSparql(sparql,ontology,pageSize,cursorType))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return  createAndFetch(this.create(request),request);
    }

    public ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request) {
        return  createAndFetch(this.create(request),request);
    }

    private ContentResponse<QueryResourceInfo> createAndFetch(ContentResponse<QueryResourceInfo> queryResourceInfoResponse, CreateQueryRequestMetadata request) {
        if (queryResourceInfoResponse.status() == SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(queryResourceInfoResponse.getData()))
                    .successPredicate(response -> false)
                    .compose();
        }

        if (request.getCreateCursorRequest() == null) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(queryResourceInfoResponse.getData()))
                    .compose();
        }

        ContentResponse<CursorResourceInfo> cursorResourceInfoResponse =
                this.cursorController.create(queryResourceInfoResponse.getData().getResourceId(), request.getCreateCursorRequest());
        if (cursorResourceInfoResponse.status() == SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl())))
                    .successPredicate(response -> false)
                    .compose();
        }

        if (request.getCreateCursorRequest().getCreatePageRequest() == null) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData())))
                    .compose();
        }

//      early exist -> in case of parameterized query content already created
        if(queryResourceInfoResponse.getData()!=null &&
                !queryResourceInfoResponse.getData().getCursorResourceInfos().isEmpty() &&
                    !queryResourceInfoResponse.getData().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty())
            return queryResourceInfoResponse;

        //
        ContentResponse<PageResourceInfo> pageResourceInfoResponse =
                this.pageController.create(
                        queryResourceInfoResponse.getData().getResourceId(),
                        cursorResourceInfoResponse.getData().getResourceId(),
                        request.getCreateCursorRequest().getCreatePageRequest());
        if (pageResourceInfoResponse.status() == SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData())))
                    .successPredicate(response -> false)
                    .compose();
        }

        cursorResourceInfoResponse.getData().setPageResourceInfos(Collections.singletonList(pageResourceInfoResponse.getData()));

        ContentResponse<Object> pageDataResponse = this.pageController.getData(
                queryResourceInfoResponse.getData().getResourceId(),
                cursorResourceInfoResponse.getData().getResourceId(),
                pageResourceInfoResponse.getData().getResourceId());

        if (pageDataResponse.status() == SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData())))
                    .successPredicate(response -> false)
                    .compose();
        }

        pageResourceInfoResponse.getData().setData(pageDataResponse.getData());
        cursorResourceInfoResponse.getData().setPageResourceInfos(Collections.singletonList(pageResourceInfoResponse.getData()));

        return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                .data(Optional.of(new QueryResourceInfo(
                        queryResourceInfoResponse.getData().getType(),
                        queryResourceInfoResponse.getData().getResourceUrl(),
                        queryResourceInfoResponse.getData().getResourceId(),
                        queryResourceInfoResponse.getData().getCursorStoreUrl(),
                        cursorResourceInfoResponse.getData())))
                .compose();

    }

    @Override
    public ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request) {
        Optional<QueryResourceInfo> queryResourceInfoResponse = driver().call(request);
        return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                .data(queryResourceInfoResponse)
                .compose();
    }

    @Override
    public ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        return Builder.builder(OK, NOT_FOUND)
                .data(driver().getNextPageData(queryId,cursorId,pageSize,deleteCurrentPage))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return Builder.<StoreResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo())
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return Builder.<QueryResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        return Builder.<Query>builder(OK, NOT_FOUND)
                .data(driver().getV1(queryId))
                .compose();
    }

    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        return Builder.<AsgQuery>builder(OK, NOT_FOUND)
                .data(driver().getAsg(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return Builder.<PlanNode<Plan>>builder(OK, NOT_FOUND)
                .data(driver().planVerbose(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return Builder.<PlanWithCost<Plan, PlanDetailedCost>>builder(OK, NOT_FOUND)
                .data(driver().explain(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return Builder.<Boolean>builder(ACCEPTED, NOT_FOUND)
                .data(driver().delete(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
            return Builder.<PlanWithCost<Plan, PlanDetailedCost>>builder(ACCEPTED, NOT_FOUND)
                .data(driver().plan(query))
                .compose();
    }

    @Override
    public ContentResponse<GraphTraversal> traversal(Query query) {
        return Builder.<GraphTraversal>builder(ACCEPTED, NOT_FOUND)
                .data(driver().traversal(query))
                .compose();
    }
    //endregion

    /**
     * replace execution driver
     * @param driver
     * @return
     */
    public StandardQueryController driver(QueryDriver driver) {
        this.driver = driver;
        return this;
    }

    //region Fields
    private QueryDriver driver;
    private CursorController cursorController;
    private PageController pageController;
    //endregion
}
