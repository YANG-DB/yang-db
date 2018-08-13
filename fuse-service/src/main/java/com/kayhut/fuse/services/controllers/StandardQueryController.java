package com.kayhut.fuse.services.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.*;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

import java.util.Collections;
import java.util.Optional;

import static com.kayhut.fuse.model.Utils.getOrCreateId;
import static org.jooby.Status.*;

/**
 * Created by lior on 19/02/2017.
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
        String queryId = getOrCreateId(request.getId());
        QueryMetadata metadata = new QueryMetadata(queryId, request.getName(), System.currentTimeMillis(), request.getTtl());

        return Builder.<QueryResourceInfo>builder(CREATED, SERVER_ERROR )
                .data(driver.create(metadata, request.getQuery()))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        ContentResponse<QueryResourceInfo> queryResourceInfoResponse = this.create(request);
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
