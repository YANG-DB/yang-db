package com.yangdb.fuse.services.modules;

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
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.rendering.LoggingJacksonRenderer;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.MediaType;
import org.jooby.Renderer;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class LoggingJacksonModule extends ModuleBase {
    //region Constructors
    public LoggingJacksonModule() {
    }

    public LoggingJacksonModule(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.enabled = true;
    }
    //endregion

    //region Module Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        if (this.enabled) {
            ObjectMapper mapper = new ObjectMapper();
            Locale locale = env.locale();
            mapper.setDateFormat(new SimpleDateFormat(config.getString("application.dateFormat"), locale));
            mapper.setLocale(locale);
            mapper.setTimeZone(TimeZone.getTimeZone(config.getString("application.tz")));
            mapper.registerModule(new Jdk8Module());
            mapper.registerModule(new JavaTimeModule());
            mapper.registerModule(new ParameterNamesModule());

            Renderer renderer = new LoggingJacksonRenderer(mapper, MediaType.json, LoggerFactory.getLogger(LoggingJacksonRenderer.class), this.metricRegistry);

            Multibinder.newSetBinder(binder, Renderer.class).addBinding().toInstance(renderer);
            binder.bind(Key.get(Renderer.class, Names.named(renderer.toString()))).toInstance(renderer);
        }
    }
    //endregion

    //region Fields
    private MetricRegistry metricRegistry;
    private boolean enabled = false;
    //endregion
}
