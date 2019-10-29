package com.yangdb.fuse.services;

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.yangdb.fuse.services.controller.PocGraphController;
import org.jooby.Jooby;
import org.jooby.Results;

public class PocControllerRegistrar extends AppControllerRegistrarBase<PocGraphController> {

    public static final String BASE = "/poc/graph";

    //region Constructors
    public PocControllerRegistrar() {
        super(PocGraphController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */

        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/rank").resourceUrl(":queryId", ":cursorId", ":pageId"),
                req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("queryId").value(),
                        req.param("cursorId").value(),
                        req.param("pageId").value(),
                        req.param("context").isSet() ? req.param("context").value() : null)));

        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/rank/report").baseUrl(),
                req -> Results.json(this.getController(app).getGraphWithRankReport(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null,
                        req.param("category").isSet() ? req.param("category").value() : null,
                        req.param("headers").isSet() ? req.param("headers").value().split(",") : new String[]{"title"})));

        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/rank").baseUrl(),
                req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null,
                        req.param("category").isSet() ? req.param("category").value() : null)));


        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/connectedComp").baseUrl(),
                req -> Results.json(this.getController(app).getGraphWithConnectedComponents(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null)));

    }
    //endregion
}
