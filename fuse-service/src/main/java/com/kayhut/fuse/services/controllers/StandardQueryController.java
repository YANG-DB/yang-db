package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.*;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.CreateQueryAndFetchRequest;

import java.util.Optional;
import org.slf4j.MDC;

import static com.kayhut.fuse.model.Utils.getOrCreateId;
import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardQueryController implements QueryController {
    //region Constructors
    @Inject
    public StandardQueryController(
            QueryDriver driver,
            CursorController cursorController,
            PageController pageController) {
        this.driver = driver;
        this.cursorController = cursorController;
        this.pageController = pageController;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        String queryId = getOrCreateId(request.getId());
        QueryMetadata metadata = new QueryMetadata(queryId, request.getName(), System.currentTimeMillis());
        //plan verbose flag
        if (request.isVerbose()) {
            MDC.put(PlanNode.PLAN_VERBOSE, "true");
        } else {
            MDC.put(PlanNode.PLAN_VERBOSE, null);
        }

        return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR )
                .data(driver.create(metadata, request.getQuery()))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryAndFetchRequest request) {
        ContentResponse<QueryResourceInfo> queryResourceInfoResponse = this.create(request);
        if (queryResourceInfoResponse.status() == SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR)
                    .data(Optional.of(queryResourceInfoResponse.getData()))
                    .successPredicate(response -> false)
                    .compose();
        }

        if (request.getCreateCursorRequest() == null) {
            return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR)
                    .data(Optional.of(queryResourceInfoResponse.getData()))
                    .compose();
        }

        ContentResponse<CursorResourceInfo> cursorResourceInfoResponse =
                this.cursorController.create(queryResourceInfoResponse.getData().getResourceId(), request.getCreateCursorRequest());
        if (cursorResourceInfoResponse.status() == SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryCursorPageResourceInfo(
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData(),
                            null
                    )))
                    .successPredicate(response -> false)
                    .compose();
        }

        if (request.getCreatePageRequest() == null) {
            return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryCursorPageResourceInfo(
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData(),
                            null)))
                    .compose();
        }

        ContentResponse<PageResourceInfo> pageResourceInfoResponse =
                this.pageController.create(
                        queryResourceInfoResponse.getData().getResourceId(),
                        cursorResourceInfoResponse.getData().getResourceId(),
                        request.getCreatePageRequest());
        if (pageResourceInfoResponse.status() == SERVER_ERROR) {
            this.delete(queryResourceInfoResponse.getData().getResourceId());
            return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR)
                    .data(Optional.of(new QueryCursorPageResourceInfo(
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData(),
                            pageResourceInfoResponse.getData())))
                    .successPredicate(response -> false)
                    .compose();
        }

        return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR)
                .data(Optional.of(new QueryCursorPageResourceInfo(
                        queryResourceInfoResponse.getData().getResourceUrl(),
                        queryResourceInfoResponse.getData().getResourceId(),
                        queryResourceInfoResponse.getData().getCursorStoreUrl(),
                        cursorResourceInfoResponse.getData(),
                        pageResourceInfoResponse.getData())))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return Builder.<StoreResourceInfo>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(this.driver.getInfo())
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return Builder.<QueryResourceInfo>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(this.driver.getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return Builder.<PlanNode<Plan>>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(this.driver.planVerbose(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return Builder.<PlanWithCost<Plan, PlanDetailedCost>>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(this.driver.explain(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return Builder.<Boolean>builder(randomUUID().toString(),ACCEPTED, NOT_FOUND)
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
