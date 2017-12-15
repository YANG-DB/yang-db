package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
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
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingQueryController implements QueryController {
    public static final String injectionName = "LoggingQueryController.inner";

    //region Constructors
    @Inject
    public LoggingQueryController(@Named(injectionName)QueryController controller) {
        this.logger = LoggerFactory.getLogger(controller.getClass());
        this.controller = controller;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start create");
            return controller.create(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed create: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish create");
            }
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryAndFetchRequest request) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start createAndFetch");
            return controller.createAndFetch(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed createAndFetch: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish createAndFetch");
            }
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo();
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start explain");
            return controller.explain(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed explain: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish explain");
            }
        }
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start planVerbose");
            return controller.planVerbose(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed planVerbose: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish planVerbose");
            }
        }
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start delete");
            return controller.delete(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed delete: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish delete");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private QueryController controller;
    //endregion
}
