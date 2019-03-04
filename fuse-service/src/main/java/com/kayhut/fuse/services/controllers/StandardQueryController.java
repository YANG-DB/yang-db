package com.kayhut.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.*;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;

import java.util.Collections;
import java.util.Optional;

import static org.jooby.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardQueryController implements QueryController {
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
                .data(driver.create(request))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR )
                .data(driver.create(request))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }

    @Override
    public ContentResponse<Object> run(Query query) {
        return Builder.builder(CREATED, SERVER_ERROR )
                .data(driver.run(query))
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
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl())))
                    .successPredicate(response -> false)
                    .compose();
        }

        if (request.getCreateCursorRequest().getCreatePageRequest() == null) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData())))
                    .compose();
        }

        ContentResponse<PageResourceInfo> pageResourceInfoResponse =
                this.pageController.create(
                        queryResourceInfoResponse.getData().getResourceId(),
                        cursorResourceInfoResponse.getData().getResourceId(),
                        request.getCreateCursorRequest().getCreatePageRequest());
        if (pageResourceInfoResponse.status() == SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
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
                        queryResourceInfoResponse.getData().getResourceUrl(),
                        queryResourceInfoResponse.getData().getResourceId(),
                        queryResourceInfoResponse.getData().getCursorStoreUrl(),
                        cursorResourceInfoResponse.getData())))
                .compose();

    }

    @Override
    public ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request) {
        Optional<QueryResourceInfo> queryResourceInfoResponse = driver.call(request);
        return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR)
                .data(queryResourceInfoResponse)
                .compose();
    }

    @Override
    public ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        return Builder.builder(OK, NOT_FOUND)
                .data(this.driver.getNextPageData(queryId,cursorId,pageSize,deleteCurrentPage))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return Builder.<StoreResourceInfo>builder(OK, NOT_FOUND)
                .data(this.driver.getInfo())
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return Builder.<QueryResourceInfo>builder(OK, NOT_FOUND)
                .data(this.driver.getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        return Builder.<Query>builder(OK, NOT_FOUND)
                .data(this.driver.getV1(queryId))
                .compose();
    }

    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        return Builder.<AsgQuery>builder(OK, NOT_FOUND)
                .data(this.driver.getAsg(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return Builder.<PlanNode<Plan>>builder(OK, NOT_FOUND)
                .data(this.driver.planVerbose(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return Builder.<PlanWithCost<Plan, PlanDetailedCost>>builder(OK, NOT_FOUND)
                .data(this.driver.explain(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return Builder.<Boolean>builder(ACCEPTED, NOT_FOUND)
                .data(this.driver.delete(queryId))
                .compose();
    }
    //endregion

    //region Fields
    private QueryDriver driver;

    private CursorController cursorController;
    private PageController pageController;
    //endregion
}
