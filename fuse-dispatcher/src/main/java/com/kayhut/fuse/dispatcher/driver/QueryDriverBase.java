package com.kayhut.fuse.dispatcher.driver;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.FuseError;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public abstract class QueryDriverBase  implements QueryDriver {
    //region Constructors
    @Inject
    public QueryDriverBase(
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            ResourceStore resourceStore,
            AppUrlSupplier urlSupplier) {
        this.queryTransformer = queryTransformer;
        this.queryValidator = queryValidator;
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region QueryDriver Implementation
    @Override
    public Optional<QueryResourceInfo> create(QueryMetadata metadata, Query query) {
        AsgQuery asgQuery = this.queryTransformer.transform(query);

        ValidationResult validationResult = this.queryValidator.validate(asgQuery);
        if (!validationResult.valid()) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            Arrays.toString(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)))));
        }

        this.resourceStore.addQueryResource(createResource(query, asgQuery, metadata));

        return Optional.of(new QueryResourceInfo(
                urlSupplier.resourceUrl(metadata.getId()),
                metadata.getId(),
                urlSupplier.cursorStoreUrl(metadata.getId())));
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

    //region Protected Abstract Methods
    protected abstract QueryResource createResource(Query query, AsgQuery asgQuery, QueryMetadata metadata);
    //endregion

    //region Fields
    private QueryTransformer<Query, AsgQuery> queryTransformer;
    private QueryValidator<AsgQuery> queryValidator;
    private ResourceStore resourceStore;
    private final AppUrlSupplier urlSupplier;
    //endregion
}
