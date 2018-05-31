package com.kayhut.fuse.services.modules.providers;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.kayhut.fuse.services.controllers.QueryController;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.typesafe.config.Config;
import org.slf4j.Logger;

/**
 * Created by Roman on 1/6/2018.
 */
public class QueryControllerProvider implements Provider<QueryController> {
    public static final String controllerParameter = "LoggingApiDescriptionController.@controller";
    public static final String loggerParameter = "LoggingApiDescriptionController.@logger";

    //region Constructors
    @Inject
    public QueryControllerProvider(
            QueryController controller,
            Logger logger,
            MetricRegistry metricRegistry) {
        this.queryController = queryController;
        this.logger = logger;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region Provider Implementation
    @Override
    public QueryController get() {
        return queryController;
    }
    //endregion

    //region Fields
    private QueryController queryController;
    private RequestIdSupplier requestIdSupplier;
    private Logger logger;
    private MetricRegistry metricRegistry;
    //endregion
}
