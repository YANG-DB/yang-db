package com.kayhut.fuse.dispatcher.driver;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import javaslang.collection.Stream;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class SimpleQueryDispatcherDriver implements QueryDispatcherDriver {
    //region Constructors
    @Inject
    public SimpleQueryDispatcherDriver(EventBus eventBus, ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region QueryDispatcherDriver Implementation
    @Override
    public Optional<QueryResourceInfo> create(QueryMetadata metadata, Query query) {
        QueryResourceInfo resourceInfo = new QueryResourceInfo(
                urlSupplier.resourceUrl(metadata.getId()),
                metadata.getId(),
                urlSupplier.cursorStoreUrl(metadata.getId()));

        submit(eventBus, new QueryCreationOperationContext(metadata, query));
        return Optional.of(resourceInfo);
    }

    @Override
    public Optional<StoreResourceInfo> getInfo() {
        Iterable<String> resourceUrls = Stream.ofAll(this.resourceStore.getQueryResources())
                .sortBy(queryResource -> queryResource.getQueryMetadata().getTime())
                .map(queryResource -> queryResource.getQueryMetadata().getId())
                .map(this.urlSupplier::resourceUrl)
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.queryStoreUrl(),null, resourceUrls));
    }

    @Override
    public Optional<QueryResourceInfo> getInfo(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        QueryResourceInfo resourceInfo = new QueryResourceInfo(urlSupplier.resourceUrl(queryId),queryId, urlSupplier.cursorStoreUrl(queryId));
        return Optional.of(resourceInfo);
    }

    @Override
    public Optional<Plan> explain(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getExecutionPlan());
    }

    @Override
    public Optional<Boolean> delete(String queryId) {
        resourceStore.deleteQueryResource(queryId);
        return Optional.of(true);
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private final ResourceStore resourceStore;
    private final AppUrlSupplier urlSupplier;
    //endregion
}
