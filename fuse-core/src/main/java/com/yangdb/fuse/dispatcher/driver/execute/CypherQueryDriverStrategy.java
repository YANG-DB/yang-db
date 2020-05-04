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
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgCompositeQuery;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.model.transport.PlanTraceOptions;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.hasInnerQuery;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.concrete;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.parameterized;

public class CypherQueryDriverStrategy extends QueryExecutionUtils<CreateJsonQueryRequest> {

    private final JsonQueryTransformerFactory transformerFactory;

    @Inject
    public CypherQueryDriverStrategy(PageDriver pageDriver,CursorDriver cursorDriver, QueryTransformer<AsgQuery, AsgQuery> queryRewriter, JsonQueryTransformerFactory transformerFactory, QueryValidator<AsgQuery> queryValidator, ResourceStore resourceStore, PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher, AppUrlSupplier urlSupplier) {
        super(pageDriver,cursorDriver, queryRewriter, queryValidator, resourceStore, planSearcher, urlSupplier);
        this.transformerFactory = transformerFactory;
    }

    @Override
    public Optional<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = this.execute(request, metadata);
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), err)));

        }
    }

    @Override
    public boolean test(CreateJsonQueryRequest request) {
        return request.getType().equals(CreateQueryRequestMetadata.TYPE_CYPHER);
    }


    protected AsgQuery transform(CreateJsonQueryRequest query) {
        return this.transformerFactory.transform(query.getType()).transform(query);
    }

}
