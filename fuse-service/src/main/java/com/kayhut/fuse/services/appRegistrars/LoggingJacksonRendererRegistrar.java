package com.kayhut.fuse.services.appRegistrars;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.rendering.JacksonLoggingRenderer;
import com.kayhut.fuse.services.modules.LoggingJacksonModule;
import org.jooby.Jooby;
import org.jooby.MediaType;
import org.jooby.Renderer;
import org.jooby.json.Jackson;
import org.slf4j.Logger;

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
