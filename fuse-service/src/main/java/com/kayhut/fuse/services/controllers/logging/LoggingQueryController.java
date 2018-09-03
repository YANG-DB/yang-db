package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.QueryController;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;
import static com.kayhut.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingQueryController extends LoggingControllerBase<QueryController> implements QueryController {
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
    public ContentResponse<StoreResourceInfo> getInfo() {
        return new LoggingSyncMethodDecorator<ContentResponse<StoreResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfo,
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
                getInfoByQueryId,
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
    //endregion

    //region Fields
    private Descriptor<Query> queryDescriptor;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter createAndFetch = MethodName.of("createAndFetch");
    private static MethodName.MDCWriter getInfo = MethodName.of("getInfo");
    private static MethodName.MDCWriter getV1ByQueryId = MethodName.of("getV1ByQueryId");
    private static MethodName.MDCWriter getAsgByQueryId = MethodName.of("getAsgByQueryId");
    private static MethodName.MDCWriter getInfoByQueryId = MethodName.of("getInfoByQueryId");
    private static MethodName.MDCWriter explain = MethodName.of("explain");
    private static MethodName.MDCWriter planVerbose = MethodName.of("planVerbose");
    private static MethodName.MDCWriter delete = MethodName.of("delete");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
