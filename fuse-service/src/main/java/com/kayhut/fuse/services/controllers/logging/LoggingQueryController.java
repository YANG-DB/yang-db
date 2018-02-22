package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.logging.RequestId;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryAndFetchRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.QueryController;
import org.slf4j.Logger;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingQueryController implements QueryController {
    public static final String controllerParameter = "LoggingQueryController.@controller";
    public static final String loggerParameter = "LoggingQueryController.@logger";

    //region Constructors
    @Inject
    public LoggingQueryController(
            @Named(controllerParameter) QueryController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), create.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start create",
                    LogType.of(start),
                    RequestIdByScope.of(request.getId()), create,
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.create(request);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed create", LogType.of(failure), create, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), create.toString(), "failure")).mark();
            throw new RuntimeException(ex);
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish create", LogType.of(success), create, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish create", LogType.of(success), create, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), "create", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryAndFetchRequest request) {
        boolean thrownException = false;

        try {
            this.logger.trace("start createAndFetch");
            return controller.createAndFetch(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed createAndFetch", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish createAndFetch");
            }
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfo.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfo",
                    LogType.of(start), getInfo,
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            this.logger.trace("start getInfo");
            return controller.getInfo();
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getInfo", LogType.of(failure), getInfo, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfo.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getInfo", LogType.of(success), getInfo, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getInfo", LogType.of(success), getInfo, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getInfo.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getInfoByQueryId.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start getInfoByQueryId",
                    LogType.of(start), getInfoByQueryId,
                    RequestIdByScope.of(queryId),
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getInfoByQueryId", LogType.of(failure), getInfoByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getInfoByQueryId", LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getInfoByQueryId", LogType.of(success), getInfoByQueryId, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getInfoByQueryId.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getV1ByQueryId.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start getV1ByQueryId",
                    LogType.of(start), getV1ByQueryId,
                    RequestIdByScope.of(queryId),
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.getV1(queryId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getV1ByQueryId", LogType.of(failure), getV1ByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getV1ByQueryId.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getV1ByQueryId", LogType.of(success), getV1ByQueryId, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getV1ByQueryId", LogType.of(success), getV1ByQueryId, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getV1ByQueryId.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }
    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), getAsgByQueryId.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start getAsgByQueryId",
                    LogType.of(start), getAsgByQueryId,
                    RequestIdByScope.of(queryId),
                    RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.getAsg(queryId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getAsgByQueryId", LogType.of(failure), getAsgByQueryId, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), getAsgByQueryId.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish getAsgByQueryId", LogType.of(success), getAsgByQueryId, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish getAsgByQueryId", LogType.of(success), getAsgByQueryId, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), getAsgByQueryId.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.trace("start explain");
            return controller.explain(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed explain", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish explain");
            }
        }
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.trace("start planVerbose");
            return controller.planVerbose(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed planVerbose", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish planVerbose");
            }
        }
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), delete.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start delete",
                    LogType.of(start), delete, RequestId.of(this.requestIdSupplier.get()), Elapsed.now(), ElapsedFrom.now()).log();
            return controller.delete(queryId);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed delete", LogType.of(failure), delete, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, info, "finish delete", LogType.of(success), delete, ElapsedFrom.now()).log();
                new LogMessage.Impl(this.logger, trace, "finish delete", LogType.of(success), delete, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), delete.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private QueryController controller;
    private RequestIdSupplier requestIdSupplier;
    private Logger logger;
    private MetricRegistry metricRegistry;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter getInfo = MethodName.of("getInfo");
    private static MethodName.MDCWriter getV1ByQueryId = MethodName.of("getV1ByQueryId");
    private static MethodName.MDCWriter getAsgByQueryId = MethodName.of("getAsgByQueryId");
    private static MethodName.MDCWriter getInfoByQueryId = MethodName.of("getInfoByQueryId");
    private static MethodName.MDCWriter delete = MethodName.of("delete");
    //endregion
}
