package com.kayhut.fuse.dispatcher.driver;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.context.QueryValidationOperationContext;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.FuseError;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class SimpleQueryDispatcherDriver implements QueryDispatcherDriver {
    //region Constructors
    @Inject
    private QueryValidationOperationContext.Processor validator;

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
        ValidationContext validationContext = validator.process(new QueryValidationOperationContext(metadata, query));

        if (validationContext.valid())
            return Optional.of(new QueryResourceInfo(
                    urlSupplier.resourceUrl(metadata.getId()),
                    metadata.getId(),
                    urlSupplier.cursorStoreUrl(metadata.getId())));
        else {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            Arrays.toString(validationContext.errors()))));
        }
    }

    @Override
    public Optional<StoreResourceInfo> getInfo() {
        Iterable<String> resourceUrls = Stream.ofAll(this.resourceStore.getQueryResources())
                .sortBy(queryResource -> queryResource.getQueryMetadata().getTime())
                .map(queryResource -> queryResource.getQueryMetadata().getId())
                .map(this.urlSupplier::resourceUrl)
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.queryStoreUrl(), null, resourceUrls));
    }

    @Override
    public Optional<QueryResourceInfo> getInfo(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        QueryResourceInfo resourceInfo = new QueryResourceInfo(urlSupplier.resourceUrl(queryId), queryId, urlSupplier.cursorStoreUrl(queryId));
        return Optional.of(resourceInfo);
    }

    @Override
    public Optional<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getExecutionPlan());
    }

    @Override
    public Optional<PlanNode<Plan>> planVerbose(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return queryResource.get().getPlanNode();
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
