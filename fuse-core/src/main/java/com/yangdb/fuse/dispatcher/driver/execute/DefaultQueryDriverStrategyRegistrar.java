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
import com.yangdb.fuse.dispatcher.epb.PlanSearcher;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DefaultQueryDriverStrategyRegistrar implements QueryStrategyRegistrar {
    private final CursorDriver cursorDriver;
    private QueryTransformer<AsgQuery, AsgQuery> queryRewriter;
    private final JsonQueryTransformerFactory transformerFactory;
    private final QueryValidator<AsgQuery> queryValidator;
    private final ResourceStore resourceStore;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    private final AppUrlSupplier urlSupplier;

    @Inject
    public DefaultQueryDriverStrategyRegistrar(
            CursorDriver cursorDriver,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            JsonQueryTransformerFactory transformerFactory,
            QueryValidator<AsgQuery> queryValidator,
            ResourceStore resourceStore,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            AppUrlSupplier urlSupplier) {

        this.cursorDriver = cursorDriver;
        this.queryRewriter = queryRewriter;
        this.transformerFactory = transformerFactory;
        this.queryValidator = queryValidator;
        this.resourceStore = resourceStore;
        this.planSearcher = planSearcher;
        this.urlSupplier = urlSupplier;
    }

    @Override
    public List<QueryDriverStrategy> register() {
        return Arrays.asList(new BaseQueryDriverStrategy(cursorDriver, queryRewriter, transformerFactory, queryValidator, resourceStore, planSearcher, urlSupplier));
    }


    @Override
    public QueryDriverStrategy apply(CreateQueryRequestMetadata request)  {
        return register().stream().filter(p -> p.test(request)).findFirst().orElseThrow((Supplier<RuntimeException>) () -> new FuseError.FuseErrorException("No Query Execution Driver found ",
                new FuseError("No Query Execution Driver found ", "No Query Execution Driver found " + request)));
    }

}

