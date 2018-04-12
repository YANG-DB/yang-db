package com.kayhut.fuse.services;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.services.appRegistrars.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple2;
import org.jooby.Jooby;
import org.jooby.RequestLogger;
import org.jooby.Results;
import org.jooby.caffeine.CaffeineCache;
import org.jooby.handlers.CorsHandler;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.scanner.Scanner;
import org.jooby.swagger.SwaggerUI;

import java.io.File;
import java.util.List;


@SuppressWarnings({"unchecked", "rawtypes"})
public class FuseApp extends Jooby {
    //region Consructors
    public FuseApp(AppUrlSupplier localUrlSupplier) {
        //log all requests
        use(new Scanner());
        use(new Jackson());
        use("*", new RequestLogger().extended());
        //metrics statistics
        MetricRegistry metricRegistry = new MetricRegistry();
        bind(metricRegistry);
        use(new Metrics(metricRegistry)
                .request()
                .threadDump()
                .ping()
                .metric("memory", new MemoryUsageGaugeSet())
                .metric("threads", new ThreadStatesGaugeSet())
                .metric("gc", new GarbageCollectorMetricSet())
                .metric("fs", new FileDescriptorRatioGauge()));

        use(use(new CaffeineCache<Tuple2<String, List<String>>, List<Statistics.BucketInfo>>() {}));
        get("", () ->  Results.redirect("/public/assets/earth.html"));
        get("/", () ->  Results.redirect("/public/assets/earth.html"));
        get("/collision", () ->  Results.redirect("/public/assets/collision.html"));
        get("swagger/swagger.json", () ->  Results.redirect("/public/assets/swagger/swagger.json"));

        //'Access-Control-Allow-Origin' header
        use("*", new CorsHandler());
        //expose html assets
        assets("public/assets/**");

        new BeforeAfterAppRegistrar().register(this, localUrlSupplier);
        new HomeAppRegistrar().register(this, localUrlSupplier);
        new CorsAppRegistrar().register(this, localUrlSupplier);
        new HealthAppRegistrar().register(this, localUrlSupplier);
        new ApiDescriptionControllerRegistrar().register(this, localUrlSupplier);
        new DataLoaderControllerRegistrar().register(this, localUrlSupplier);
        new CatalogControllerRegistrar().register(this, localUrlSupplier);
        new QueryControllerRegistrar().register(this, localUrlSupplier);
        new CursorControllerRegistrar().register(this, localUrlSupplier);
        new PageControllerRegistrar().register(this, localUrlSupplier);
        new SearchControllerRegistrar().register(this, localUrlSupplier);
        new InternalsControllerRegistrar().register(this, localUrlSupplier);
        new IdGeneratorControllerRegistrar().register(this, localUrlSupplier);
        new SwaggerUI()
                .filter(route -> route.pattern().startsWith("/fuse"))
                .install(this);
    }
    //endregion

    //region Public Methods
    public FuseApp conf(File file, String activeProfile) {
        Config config = ConfigFactory.parseFile(file);
        config = config.withValue("application.profile", ConfigValueFactory.fromAnyRef(activeProfile, "FuseApp"));

        super.use(config);
        return this;
    }
    //endregion
}
