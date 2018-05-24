package com.kayhut.fuse.services.modules;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.rendering.JacksonLoggingRenderer;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;
import org.jooby.MediaType;
import org.jooby.Renderer;
import org.slf4j.Logger;
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

            Renderer renderer = new JacksonLoggingRenderer(mapper, MediaType.json, LoggerFactory.getLogger(JacksonLoggingRenderer.class), this.metricRegistry);

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
