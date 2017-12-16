package com.kayhut.fuse.executor.driver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.dispatcher.driver.QueryDriverBase;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.validation.QueryValidation;
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
public class StandardQueryDriver extends QueryDriverBase {
    //region Constructors
    @Inject
    public StandardQueryDriver(
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            ResourceStore resourceStore,
            AppUrlSupplier urlSupplier) {
        super(queryTransformer, queryValidator, resourceStore, urlSupplier);
        this.queryRewriter = queryRewriter;
        this.planSearcher = planSearcher;
    }
    //endregion

    //region QueryDriverBase Implementation
    @Override
    protected QueryResource createResource(Query query, AsgQuery asgQuery, QueryMetadata metadata) {
        AsgQuery newAsgQuery = this.queryRewriter.transform(asgQuery);
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = this.planSearcher.search(newAsgQuery);
        return new QueryResource(query, newAsgQuery, metadata, planWithCost, null);
    }
    //endregion

    //region Fields
    private QueryTransformer<AsgQuery, AsgQuery> queryRewriter;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    //endregion
}
