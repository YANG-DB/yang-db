package com.kayhut.fuse.services.modules.providers;

import com.google.inject.Provider;
import com.kayhut.fuse.services.controllers.QueryController;
import com.typesafe.config.Config;

/**
 * Created by Roman on 1/6/2018.
 */
public class QueryControllerProvider implements Provider<QueryController> {
    //region Constructors
    public QueryControllerProvider(QueryController queryController, boolean enableLogging, boolean enableMetrics) {
        this.queryController = queryController;
        this.enableLogging = enableLogging;
        this.enableMetrics = enableMetrics;
    }
    //endregion

    //region Provider Implementation
    @Override
    public QueryController get() {
        return null;
    }
    //endregion

    //region Fields
    private QueryController queryController;
    private boolean enableLogging;
    private boolean enableMetrics;
    //endregion
}
