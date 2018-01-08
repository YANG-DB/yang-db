package com.kayhut.fuse.services.controllers.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.logging.RequestIdConverter;
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
import org.slf4j.MDC;

import static com.codahale.metrics.MetricRegistry.name;

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
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "create")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start create");
            return controller.create(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed create", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "create", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish create");
                this.metricRegistry.meter(name(this.logger.getName(), "create", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryAndFetchRequest request) {
        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
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
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfo")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo();
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
                this.metricRegistry.meter(name(this.logger.getName(), "getInfo", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "getInfoByQueryId")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryId", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
                this.metricRegistry.meter(name(this.logger.getName(), "getInfoByQueryId", "success")).mark();
            }
            timerContext.stop();
        }
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
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
        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
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
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "delete")).time();

        MDC.put(RequestIdConverter.key, this.requestIdSupplier.get());
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start delete");
            return controller.delete(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed delete", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "delete", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish delete");
                this.metricRegistry.meter(name(this.logger.getName(), "delete", "success")).mark();
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
    //endregion
}
