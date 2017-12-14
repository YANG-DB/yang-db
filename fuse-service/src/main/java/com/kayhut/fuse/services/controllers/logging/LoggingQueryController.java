package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingQueryController implements QueryController {
    public static final String injectionName = "LoggingQueryController.inner";

    //region Constructors
    @Inject
    public LoggingQueryController(@Named(injectionName)QueryController controller) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.controller = controller;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        boolean thrownException = false;

        try {
            this.logger.debug("start create");
            return controller.create(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed create: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish create");
            }
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryAndFetchRequest request) {
        boolean thrownException = false;

        try {
            this.logger.debug("start createAndFetch");
            return controller.createAndFetch(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed createAndFetch: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish createAndFetch");
            }
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        boolean thrownException = false;

        try {
            this.logger.debug("start getInfo");
            return controller.getInfo();
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.debug("start getInfo");
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.debug("start explain");
            return controller.explain(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed explain: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish explain");
            }
        }
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.debug("start planVerbose");
            return controller.planVerbose(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed planVerbose: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish planVerbose");
            }
        }
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.debug("start delete");
            return controller.delete(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed delete: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish delete");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private QueryController controller;
    //endregion
}
