package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.QueryDispatcherDriver;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

import static com.kayhut.fuse.model.Utils.getOrCreateId;
import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleQueryController implements QueryController {

    private EventBus eventBus;
    private QueryDispatcherDriver driver;

    @Inject
    public SimpleQueryController( EventBus eventBus, QueryDispatcherDriver driver ) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.driver = driver;
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        String queryId = getOrCreateId(request.getId());
        QueryMetadata metadata = new QueryMetadata(queryId, request.getName(), System.currentTimeMillis());

        return Builder.<QueryResourceInfo>builder(request.getId(), CREATED, SERVER_ERROR )
                .data(driver.create(metadata, request.getQuery()))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return Builder.<StoreResourceInfo>builder(randomUUID().toString(),FOUND, NOT_FOUND)
                .data(this.driver.getInfo())
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return Builder.<QueryResourceInfo>builder(randomUUID().toString(),FOUND, NOT_FOUND)
                .data(this.driver.getInfo(queryId))
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
}
