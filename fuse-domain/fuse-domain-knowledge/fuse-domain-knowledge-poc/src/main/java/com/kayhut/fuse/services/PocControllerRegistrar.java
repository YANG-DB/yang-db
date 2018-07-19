package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.kayhut.fuse.services.controller.PocGraphController;
import io.restassured.internal.http.URIBuilder;
import javaslang.Tuple2;
import org.jooby.Jooby;
import org.jooby.Results;

import java.util.Arrays;
import java.util.Objects;

public class PocControllerRegistrar extends AppControllerRegistrarBase<PocGraphController> {
    //region Constructors
    public PocControllerRegistrar() {
        super(PocGraphController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */
        DefaultAppUrlSupplier pocUrlSupplier = new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + "/poc/graph");

        app.use(pocUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"))
                .get(req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("queryId").value(),
                        req.param("cursorId").value(),
                        req.param("pageId").value(),
                        req.param("context").isSet() ? req.param("context").value() : null)));

        app.use(pocUrlSupplier.baseUrl())
                .get(req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null)));

        /** view the selected graph with d3 html*/
        app.use(pocUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/view")
                .get(req -> Results.redirect("/public/assets/ResultsTreeViewer.html?q=" +
                        "fuse/poc/graph/query/" + req.param("queryId").value() + "/cursor/"
                        + req.param("cursorId").value()
                        + "/page/" + req.param("pageId").value() +"?" +
                        (req.param("cache").isSet() ? ("cache="+req.param("cache").booleanValue()) : ("cache=false")) + ";" +
                        (req.param("context").isSet() ? ("context="+req.param("context").toString()) : "")));

        /** view the entire graph with d3 html*/
        app.use(pocUrlSupplier.baseUrl() + "/view")
                .get(req -> Results.redirect("/public/assets/ResultsTreeViewer.html?q=" +
                        "fuse/poc/graph?" +
                        (req.param("cache").isSet() ? ("cache="+req.param("cache").booleanValue()) : ("cache=false")) + ";" +
                        (req.param("count").isSet() ? ("count="+req.param("count").intValue()) :  ("count=-1")) + ";" +
                        (req.param("context").isSet() ? ("context="+req.param("context").toString()) : "")));
    }
    //endregion
}
