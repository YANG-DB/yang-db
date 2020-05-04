package com.yangdb.fuse.dispatcher.driver.execute;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.yangdb.fuse.dispatcher.driver.CursorDriver;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.epb.PlanSearcher;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;

import java.util.Optional;

public class V1QueryDriverStrategy extends QueryExecutionUtils<CreateQueryRequest> {
    private QueryTransformer<Query, AsgQuery> queryTransformer;

    @Inject
    public V1QueryDriverStrategy(
            PageDriver pageDriver,
            CursorDriver cursorDriver,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            ResourceStore resourceStore,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            AppUrlSupplier urlSupplier) {
        super(pageDriver, cursorDriver, queryRewriter, queryValidator, resourceStore, planSearcher, urlSupplier);
        this.queryTransformer = queryTransformer;
    }

    @Override
    public boolean test(CreateQueryRequest request) {
        return request.getType().equals(CreateQueryRequestMetadata.TYPE_V1_QUERY);
    }

    @Override
    protected AsgQuery transform(CreateQueryRequest query) {
        return this.queryTransformer.transform(query.getQuery());
    }

    @Override
    public Optional<QueryResourceInfo> create(CreateQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = this.execute(request, metadata);
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err)));

        }
    }
}
