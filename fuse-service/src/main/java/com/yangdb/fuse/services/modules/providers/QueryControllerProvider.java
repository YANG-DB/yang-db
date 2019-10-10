package com.yangdb.fuse.services.modules.providers;

/*-
 *
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.yangdb.fuse.services.controllers.QueryController;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
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
