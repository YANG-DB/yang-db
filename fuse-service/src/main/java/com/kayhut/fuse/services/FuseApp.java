package com.kayhut.fuse.services;

import com.cedarsoftware.util.io.JsonWriter;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import org.jooby.Jooby;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.scanner.Scanner;

import java.util.Optional;


@SuppressWarnings({"unchecked", "rawtypes"})
public class FuseApp extends Jooby {


    private static Result HOME = Results
            .ok(
                    "<!doctype html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "  <title>Fuse API</title>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "<h1>Fuse API</h1>\n" +
                            "<ul>\n" +
                            "<li>Resource Url: <a href=\"/fuse\">fuse</a></li>\n" +
                            "<li>Health Url: <a href=\"/fuse/healthUrl\">healthUrl</a></li>\n" +
                            "<li>Query Store Url: <a href=\"/fuse/query\">queryStoreUrl</a></li>\n" +
                            "<li>Search Store Url: <a href=\"/fuse/search\">searchStoreUrl</a></li>\n" +
                            "<li>Catalog Store Url: <a href=\"/fuse/catalog\">catalogStoreUrl</a></li>\n" +
                            "</ul>\n" +
                            "<p>More at <a href=\"http://sheker.com\">" +
                            "Sheker</a>\n" +
                            "</body>\n" +
                            "</html>")
            .type("html");

    //region Consructors
    public FuseApp(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        //metrics statistics
        use(new Metrics()
                .request()
                .threadDump()
                .ping()
                .metric("memory", new MemoryUsageGaugeSet())
                .metric("threads", new ThreadStatesGaugeSet())
                .metric("gc", new GarbageCollectorMetricSet())
                .metric("fs", new FileDescriptorRatioGauge()));

        use(new Scanner());
        use(new Jackson());
        get("/", () -> HOME);

        registerCors(localUrlSupplier, publicUrlSupplier);
        registerFuseApiDescription(localUrlSupplier, publicUrlSupplier);
        registerHealthApi(localUrlSupplier, publicUrlSupplier);
        registerCatalogApi(localUrlSupplier, publicUrlSupplier);
        registerQueryApi(localUrlSupplier, publicUrlSupplier);
        registerCursorApi(localUrlSupplier, publicUrlSupplier);
        registerPageApi(localUrlSupplier, publicUrlSupplier);
        registerSearchApi(localUrlSupplier, publicUrlSupplier);
    }
    //endregion

    //region Public Methods
    public FuseApp conf(String path, String activeProfile) {
        Config config = ConfigFactory.parseResources(path);
        config = config.withValue("application.profile", ConfigValueFactory.fromAnyRef(activeProfile, "FuseApp"));

        super.use(config);
        return this;
    }
    //endregion

    //region Controllers
    private QueryController queryCtrl() {
        return require(QueryController.class);
    }

    private CursorController cursorCtrl() {
        return require(CursorController.class);
    }

    private CatalogController catalogCtrl() {
        return require(CatalogController.class);
    }

    private SearchController searchCtrl() {
        return require(SearchController.class);
    }

    private PageController pageCtrl() {
        return require(PageController.class);
    }
    //endregion

    //region Private Methods
    private void registerCors(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        /** CORS: */
        use("*", (req, rsp) -> {
            rsp.header("Access-Control-Allow-Origin", "*");
            rsp.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH");
            rsp.header("Access-Control-Max-Age", "3600");
            rsp.header("Access-Control-Allow-Headers", "x-requested-with", "origin", "content-type",
                    "accept");
            if (req.method().equalsIgnoreCase("options")) {
                rsp.end();
            }
        });
    }

    private void registerFuseApiDescription(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        use("/fuse")
                .get(() -> new FuseResourceInfo(
                        "/fuse",
                        "/fuse/health",
                        publicUrlSupplier.queryStoreUrl(),
                        "/fuse/search",
                        publicUrlSupplier.catalogStoreUrl()));
    }

    private void registerHealthApi(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        /** get the health status of the service */
        use("/fuse/health")
                /** check health */
                .get(() -> "Alive And Well...");
    }

    private void registerCatalogApi(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        /** get the ontology by id */
        use("/fuse/catalog/ontology/:id")
                /** check health */
                .get(req -> {
                    ContentResponse response = catalogCtrl().get(req.param("id").value());
                    return Results.with(response, response.status());
                });
    }

    private void registerQueryApi(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        /** get the query store info */
        use(localUrlSupplier.queryStoreUrl())
                .get(req -> Results.with(queryCtrl().getInfo(), Status.FOUND));

        /** create a query */
        use(localUrlSupplier.queryStoreUrl())
                .post(req -> {
                    ContentResponse<QueryResourceInfo> entity = queryCtrl().create(req.body(CreateQueryRequest.class));
                    return Results.with(entity, entity.status());
                });

        /** get the query info */
        use(localUrlSupplier.resourceUrl(":queryId"))
                .get(req -> {
                    ContentResponse response = queryCtrl().getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** delete a query */
        use(localUrlSupplier.resourceUrl(":queryId"))
                .delete(req -> {
                    ContentResponse response = queryCtrl().delete(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** get the query execution plan */
        use(localUrlSupplier.resourceUrl(":queryId") + "/plan")
                .get(req -> {
                    ContentResponse response = queryCtrl().explain(req.param("queryId").value());
                    //temporary fix for jason serialization of object graphs
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });
    }

    private void registerCursorApi(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        /** get the query cursor store info */
        use(localUrlSupplier.cursorStoreUrl(":queryId"))
                .get(req -> {
                    ContentResponse response = cursorCtrl().getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** create a query cursor */
        use(localUrlSupplier.cursorStoreUrl(":queryId"))
                .post(req -> {
                    ContentResponse response = cursorCtrl().create(req.param("queryId").value(), req.body(CreateCursorRequest.class));
                    return Results.with(response, response.status());
                });

        /** get the cursor resource info */
        use(localUrlSupplier.resourceUrl(":queryId", ":cursorId"))
                .get(req -> {
                    ContentResponse response = cursorCtrl().getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        use(localUrlSupplier.resourceUrl(":queryId", ":cursorId"))
                .delete(req -> {
                    ContentResponse response = cursorCtrl().delete(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });
    }

    private void registerPageApi(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        /** get the cursor page store info*/
        use(localUrlSupplier.pageStoreUrl(":queryId", ":cursorId"))
                .get(req -> {
                    ContentResponse response = pageCtrl().getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        /** create the next page */
        use(localUrlSupplier.pageStoreUrl(":queryId", ":cursorId"))
                .post(req -> {
                    ContentResponse<PageResourceInfo> entity = pageCtrl().create(req.param("queryId").value(), req.param("cursorId").value(), req.body(CreatePageRequest.class));
                    return Results.with(entity, entity.status());
                });

        /** get page info by id */
        use(localUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"))
                .get(req -> {
                    ContentResponse response = pageCtrl().getInfo(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by id */
        use(localUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/data")
                .get(req -> {
                    ContentResponse response = pageCtrl().getData(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });
    }

    private void registerSearchApi(AppUrlSupplier localUrlSupplier, AppUrlSupplier publicUrlSupplier) {
        /** submit a search */
        use("/fuse/search")
                .post(req -> {
                    ContentResponse search = searchCtrl().search(req.body(CreateQueryRequest.class));
                    return Results.with(search, search.status());
                });
    }
    //endregion
}
