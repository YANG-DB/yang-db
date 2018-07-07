package com.kayhut.fuse.services.appRegistrars;

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
