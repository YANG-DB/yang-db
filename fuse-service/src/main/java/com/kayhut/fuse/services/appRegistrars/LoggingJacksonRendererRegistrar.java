package com.kayhut.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.services.modules.LoggingJacksonModule;
import org.jooby.Jooby;
import org.jooby.json.Jackson;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class LoggingJacksonRendererRegistrar implements AppRegistrar {
    //region Constructors
    public LoggingJacksonRendererRegistrar(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region AppRegistrar Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.use(new LoggingJacksonModule(this.metricRegistry));
        app.use(new Jackson());
    }
    //endregion

    //region Fields
    protected MetricRegistry metricRegistry;
    //endregion
}
