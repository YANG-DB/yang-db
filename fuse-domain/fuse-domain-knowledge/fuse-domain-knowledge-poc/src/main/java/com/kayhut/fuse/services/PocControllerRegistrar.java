package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.kayhut.fuse.services.controller.PocGraphController;
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

        app.use(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE+"/rank").resourceUrl(":queryId", ":cursorId", ":pageId"))
                .get(req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("queryId").value(),
                        req.param("cursorId").value(),
                        req.param("pageId").value(),
                        req.param("context").isSet() ? req.param("context").value() : null)));

        app.use(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE+"/rank").baseUrl())
                .get(req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null)));

        app.use(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE+"/connectedComp").baseUrl())
                .get(req -> Results.json(this.getController(app).getGraphWithConnectedComponents(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null)));

        app.use(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE+"/connectivity").baseUrl())
                .get(req -> Results.json(this.getController(app).getGraphWithConnectivity(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("context").isSet() ? req.param("context").value() : null)));

        app.use(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE+"/ecentrality").baseUrl())
                .get(req -> Results.json(this.getController(app).getGraphWithEccentricity(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("context").isSet() ? req.param("context").value() : null)));

        /** view the selected graph with d3 html*/
        app.use(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE+"/rank").resourceUrl(":queryId", ":cursorId", ":pageId") + "/view")
                .get(req -> Results.redirect("/public/assets/ResultsTreeViewer.html?q=" +
                        "fuse/poc/graph/rank/query/" + req.param("queryId").value() + "/cursor/"
                        + req.param("cursorId").value()
                        + "/page/" + req.param("pageId").value() +"?" +
                        (req.param("cache").isSet() ? ("cache="+req.param("cache").booleanValue()) : ("cache=false")) + ";" +
                        (req.param("context").isSet() ? ("context="+req.param("context").toString()) : "")));

        /** view the entire graph with d3 html*/
        app.use(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE+"/rank").baseUrl() + "/view")
                .get(req -> Results.redirect("/public/assets/ResultsTreeViewer.html?q=" +
                        "fuse/poc/graph/rank?" +
                        (req.param("cache").isSet() ? ("cache="+req.param("cache").booleanValue()) : ("cache=false")) + ";" +
                        (req.param("count").isSet() ? ("count="+req.param("count").intValue()) :  ("count=-1")) + ";" +
                        (req.param("context").isSet() ? ("context="+req.param("context").toString()) : "")));
    }
    //endregion
}
