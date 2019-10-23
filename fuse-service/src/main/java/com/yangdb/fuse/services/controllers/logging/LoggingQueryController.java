package com.yangdb.fuse.services.controllers.logging;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.driver.QueryDriver;
import com.yangdb.fuse.dispatcher.logging.*;
import com.yangdb.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.planTree.PlanNode;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.ExecuteStoredQueryRequest;
import com.yangdb.fuse.model.validation.ValidationResult;
import com.yangdb.fuse.services.controllers.QueryController;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.yangdb.fuse.dispatcher.logging.LogType.*;
import static com.yangdb.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingQueryController extends LoggingControllerBase<QueryController> implements QueryController<QueryController,QueryDriver> {
    public static final String controllerParameter = "LoggingQueryController.@controller";
    public static final String loggerParameter = "LoggingQueryController.@logger";
    public static final String queryDescriptorParameter = "LoggingQueryController.@queryDescriptor";

    //region Constructors
    @Inject
    public LoggingQueryController(
            @Named(controllerParameter) QueryController controller,
            @Named(loggerParameter) Logger logger,
            @Named(queryDescriptorParameter) Descriptor<Query> queryDescriptor,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
        this.queryDescriptor = queryDescriptor;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                create,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), create)
                                .with(this.queryDescriptor.describe(request.getQuery())).log();
                    }
                    return this.controller.create(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                create,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), create)
                                .with(request.getQuery()).log();
                    }
                    return this.controller.create(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> run(Query query) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                run,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (query != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(query)).log();
                    }
                    return this.controller.run(query);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<ValidationResult> validate(Query query) {
        return new LoggingSyncMethodDecorator<ContentResponse<ValidationResult>>(
                this.logger,
                this.metricRegistry,
                validate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (query != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(query)).log();
                    }
                    return this.controller.validate(query);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> run(String cypher, String ontology) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                run,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (cypher != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(cypher).log();
                    }
                    return this.controller.run(cypher,ontology );
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                createAndFetch,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(request.getQuery())).log();
                    }
                    return this.controller.createAndFetch(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                createAndFetch,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(request.getQuery()).log();
                    }
                    return this.controller.createAndFetch(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                callAndFetch,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(request.getQuery())).log();
                    }
                    return this.controller.callAndFetch(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                fetchNextPage,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.fetchNextPage(queryId,cursorId,pageSize,deleteCurrentPage), this.resultHandler());
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return new LoggingSyncMethodDecorator<ContentResponse<StoreResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfoAll,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(), this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfo,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Query>>(
                this.logger,
                this.metricRegistry,
                getV1ByQueryId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getV1(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<AsgQuery>>(
                this.logger,
                this.metricRegistry,
                getAsgByQueryId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getAsg(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<PlanWithCost<Plan, PlanDetailedCost>>>(
                this.logger,
                this.metricRegistry,
                explain,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.explain(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<PlanNode<Plan>>>(
                this.logger,
                this.metricRegistry,
                planVerbose,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.planVerbose(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Boolean>>(
                this.logger,
                this.metricRegistry,
                delete,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.delete(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
        return new LoggingSyncMethodDecorator<ContentResponse<PlanWithCost<Plan, PlanDetailedCost>>>(
                this.logger,
                this.metricRegistry,
                plan,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query.getName())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.plan(query), this.resultHandler());
    }

    @Override
    public ContentResponse<GraphTraversal> traversal(Query query) {
        return new LoggingSyncMethodDecorator<ContentResponse<GraphTraversal>>(
                this.logger,
                this.metricRegistry,
                traversal,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query.getName())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.traversal(query), this.resultHandler());
    }


    @Override
    public QueryController driver(QueryDriver driver) {
        return (QueryController) this.controller.driver(driver);
    }
    //endregion

    //region Fields
    private Descriptor<Query> queryDescriptor;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter createAndFetch = MethodName.of("createAndFetch");
    private static MethodName.MDCWriter getInfo = MethodName.of("getInfo");
    private static MethodName.MDCWriter getV1ByQueryId = MethodName.of("getV1ByQueryId");
    private static MethodName.MDCWriter getAsgByQueryId = MethodName.of("getAsgByQueryId");
    private static MethodName.MDCWriter traversal = MethodName.of("traversal");
    private static MethodName.MDCWriter explain = MethodName.of("explain");
    private static MethodName.MDCWriter planVerbose = MethodName.of("planVerbose");
    private static MethodName.MDCWriter delete = MethodName.of("delete");
    private static MethodName.MDCWriter plan = MethodName.of("plan");
    private static MethodName.MDCWriter validate = MethodName.of("validate");
    private static MethodName.MDCWriter run = MethodName.of("run");
    private static MethodName.MDCWriter callAndFetch = MethodName.of("callAndFetch");
    private static MethodName.MDCWriter getInfoAll = MethodName.of("getInfoAll");
    private static MethodName.MDCWriter fetchNextPage = MethodName.of("fetchNextPage");

    private static LogMessage.MDCWriter sequence = Sequence.incr();

    //endregion
}
