package com.kayhut.fuse.services;

import com.cedarsoftware.util.io.JsonWriter;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple2;
import org.jooby.*;
import org.jooby.caffeine.CaffeineCache;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.scanner.Scanner;

import java.io.File;
import java.util.List;


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
                            "<li>Health Url: <a href=\"/fuse/health\">healthUrl</a></li>\n" +
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
    public FuseApp(AppUrlSupplier localUrlSupplier) {
        //log all requests
        use("*", new RequestLogger().extended());
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
        use(use(new CaffeineCache<Tuple2<String, List<String>>, List<Statistics.BucketInfo>>() {
        }));
        get("/", () -> HOME);

        registerCors(localUrlSupplier);
        registerFuseApiDescription(localUrlSupplier);
        registerHealthApi(localUrlSupplier);
        registerCatalogApi(localUrlSupplier);
        registerQueryApi(localUrlSupplier);
        registerCursorApi(localUrlSupplier);
        registerPageApi(localUrlSupplier);
        registerSearchApi(localUrlSupplier);

        get("fuse/detailedPlan/Q1", () ->
        {
            String dummy = String.valueOf(System.currentTimeMillis());
            String json = "{\"name\":\"" + dummy.substring(dummy.length() - 5) + "\",\"desc\":\"[EntityOp(Asg(ETyped(1)))]\",\"children\":[{\"name\":\"1\",\"desc\":\"[EntityOp(Asg(ETyped(1))):RelationOp(Asg(Rel(2))):EntityOp(Asg(ETyped(3)))]\",\"children\":[{\"name\":\"3\",\"desc\":\"[EntityOp(Asg(ETyped(1))):RelationOp(Asg(Rel(2))):EntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(3))):EntityOp(Asg(ETyped(5)))]\",\"invalidReason\":\"blah\"}],\"invalidReason\":\"\"},{\"name\":\"2\",\"desc\":\"[EntityOp(Asg(ETyped(1))):RelationOp(Asg(Rel(4))):EntityOp(Asg(ETyped(5)))]\",\"children\":[{\"name\":\"4\",\"desc\":\"[EntityOp(Asg(ETyped(1))):RelationOp(Asg(Rel(4))):EntityOp(Asg(ETyped(5))):RelationOp(Asg(Rel(4))):EntityOp(Asg(ETyped(5)))]\",\"invalidReason\":\"sasaa\"}],\"invalidReason\":\"not valid because blah\"}],\"invalidReason\":\"\"}";
            return Results.with(json, 200)
                    .type("application/json");
        });
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

    //region Controllers
    private ApiDescriptionController apiDescriptionCtrl() {
        return require(ApiDescriptionController.class);
    }

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
    private void registerCors(AppUrlSupplier localUrlSupplier) {
        /** CORS: */
        use("*", (req, rsp) -> {
            rsp.header("Access-Control-Allow-Origin", "*");
            rsp.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH");
            rsp.header("Access-Control-Max-Age", "3600");
            rsp.header("Access-Control-Allow-Headers", "x-requested-with", "origin", "content-type", "accept");
            if (req.method().equalsIgnoreCase("options")) {
                rsp.end();
            }
        });
    }

    private void registerFuseApiDescription(AppUrlSupplier localUrlSupplier) {
        use("/fuse")
                .get(() -> apiDescriptionCtrl().getInfo());
    }

    private void registerHealthApi(AppUrlSupplier localUrlSupplier) {
        /** get the health status of the service */
        use("/fuse/health")
                /** check health */
                .get(() -> "Alive And Well...");
    }

    private void registerCatalogApi(AppUrlSupplier localUrlSupplier) {
        /** get the ontology by id */
        use("/fuse/catalog/ontology/:id")
                /** check health */
                .get(req -> {
                    ContentResponse response = catalogCtrl().get(req.param("id").value());
                    return Results.with(response, response.status());
                });
    }

    private void registerQueryApi(AppUrlSupplier localUrlSupplier) {
        /** get the query store info */
        use(localUrlSupplier.queryStoreUrl())
                .get(req -> Results.with(queryCtrl().getInfo(), Status.OK));

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
        use(localUrlSupplier.resourceUrl(":queryId") + "/planVerbose")
                .get(req -> {
                    ContentResponse response = queryCtrl().planVerbose(req.param("queryId").value());
                    //temporary fix for jason serialization of object graphs
                    return Results.with(new ObjectMapper().writeValueAsString(response.getData()), response.status()).type("application/json");
                });

        /** get the query plan verbose */
        use(localUrlSupplier.resourceUrl(":queryId") + "/plan")
                .get(req -> {
                    ContentResponse response = queryCtrl().explain(req.param("queryId").value());
                    //temporary fix for jason serialization of object graphs
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });
    }

    private void registerCursorApi(AppUrlSupplier localUrlSupplier) {
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

    private void registerPageApi(AppUrlSupplier localUrlSupplier) {
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

    private void registerSearchApi(AppUrlSupplier localUrlSupplier) {
        /** submit a search */
        use("/fuse/search")
                .post(req -> {
                    ContentResponse search = searchCtrl().search(req.body(CreateQueryRequest.class));
                    return Results.with(search, search.status());
                });
    }
    //endregion
}
