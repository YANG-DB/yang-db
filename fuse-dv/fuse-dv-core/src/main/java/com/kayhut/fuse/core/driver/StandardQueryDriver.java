package com.kayhut.fuse.core.driver;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.dispatcher.driver.PageDriver;
import com.kayhut.fuse.dispatcher.driver.QueryDriverBase;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.FuseError;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.Optional;

import java.util.List;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardQueryDriver extends QueryDriverBase {
    //region Constructors
    @Inject
    public StandardQueryDriver(
            CursorDriver cursorDriver,
            PageDriver pageDriver,
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            QueryTransformer<String, AsgQuery> jsonQueryTransformer,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            ResourceStore resourceStore,
            AppUrlSupplier urlSupplier) {
        super(cursorDriver, pageDriver, queryTransformer, jsonQueryTransformer, queryValidator, resourceStore, urlSupplier);
        this.queryRewriter = queryRewriter;
        this.planSearcher = planSearcher;
    }
    //endregion

    //region QueryDriverBase Implementation
    @Override
    protected QueryResource createResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata metadata) {
        AsgQuery newAsgQuery = rewrite(asgQuery);
        ValidationResult result = validateAsgQuery(newAsgQuery);
        if (!result.valid()) {
            throw new FuseError.FuseErrorException(new FuseError(Query.class.getSimpleName(),
                    "Asg Query rewrite validation error " + result.toString()));
        }

        PlanWithCost<Plan, PlanDetailedCost> planWithCost = planWithCost(metadata, newAsgQuery);

        return new QueryResource(request, query, newAsgQuery, metadata, planWithCost, null);
    }

    protected PlanWithCost<Plan, PlanDetailedCost> planWithCost(QueryMetadata metadata, AsgQuery query) {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = PlanWithCost.EMPTY_PLAN;

        //calculate execution plan
        if (metadata.isSearchPlan()) {
            planWithCost = this.planSearcher.search(query);

            if (planWithCost == null) {
                throw new IllegalStateException("No valid plan was found for query " + (AsgQueryDescriptor.toString(query)));
            }
        }
        return planWithCost;
    }

    protected AsgQuery rewrite(AsgQuery asgQuery) {
        return this.queryRewriter.transform(asgQuery);
    }
    //endregion

    //region Fields
    private QueryTransformer<AsgQuery, AsgQuery> queryRewriter;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;


    //endregion
}
