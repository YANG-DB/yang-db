package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.QueryDispatcherDriver;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.*;

import java.util.Optional;
import java.util.UUID;

import static com.kayhut.fuse.model.Utils.getOrCreateId;

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
        Optional<QueryResourceInfo> resourceInfo = driver.create(metadata, request.getQuery());

        return ContentResponse.Builder.<QueryResourceInfo>builder(request.getId())
                .data(resourceInfo.get())
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        Optional<StoreResourceInfo> resourceInfo = this.driver.getInfo();
        if (!resourceInfo.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<StoreResourceInfo>builder(UUID.randomUUID().toString())
                .data(resourceInfo.get()).compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        Optional<QueryResourceInfo> resourceInfo = this.driver.getInfo(queryId);
        if (!resourceInfo.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<QueryResourceInfo>builder(UUID.randomUUID().toString())
                .data(resourceInfo.get()).compose();
    }

    @Override
    public ContentResponse<Plan> explain(String queryId) {
        Optional<Plan> plan = this.driver.explain(queryId);
        if (!plan.isPresent()) {
            ContentResponse.Builder.<Plan>builder(UUID.randomUUID().toString()).data(null).compose();
        }

        return ContentResponse.Builder.<Plan>builder(UUID.randomUUID().toString()).data(plan.get()).compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        Optional<Boolean> isDeleted = this.driver.delete(queryId);
        if (!isDeleted.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<Boolean>builder(UUID.randomUUID().toString())
                .data(isDeleted.get()).compose();
    }
}
