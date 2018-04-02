package com.kayhut.fuse.services;

import com.cedarsoftware.util.io.JsonWriter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.PlanWithCostDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.*;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple2;
import org.jooby.*;
import org.jooby.caffeine.CaffeineCache;
import org.jooby.handlers.CorsHandler;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.scanner.Scanner;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;


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

        use(new Scanner());
        use(new Jackson());
        use(use(new CaffeineCache<Tuple2<String, List<String>>, List<Statistics.BucketInfo>>() {
        }));
        get("/", () -> HOME);
        //'Access-Control-Allow-Origin' header
        use("*", new CorsHandler());
        //expose html assets
        assets("public/assets/**");


        registerCors(localUrlSupplier);
        registerFuseApiDescription(localUrlSupplier);
        registerHealthApi(localUrlSupplier);
        registerDataLoaderApi(localUrlSupplier);
        registerCatalogApi(localUrlSupplier);
        registerQueryApi(localUrlSupplier);
        registerCursorApi(localUrlSupplier);
        registerPageApi(localUrlSupplier);
        registerSearchApi(localUrlSupplier);
        registerInternals(localUrlSupplier);
        registerIdGenerator(localUrlSupplier);
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

    private DataLoaderController dataLoaderCtrl() {
        return require(DataLoaderController.class);
    }

    private InternalsController internalsController() {
        return require(InternalsController.class);
    }

    private SearchController searchCtrl() {
        return require(SearchController.class);
    }

    private PageController pageCtrl() {
        return require(PageController.class);
    }

    private IdGeneratorController idGeneratorCtrl() {
        return require(new TypeLiteral<IdGeneratorController<Object>>(){});
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

    private void registerInternals(AppUrlSupplier localUrlSupplier) {
        /** get the health status of the service */
        use("/fuse/internal/statisticsProvider/name")
                .get(req -> Results.with(internalsController().getStatisticsProviderName()));
        use("/fuse/internal/statisticsProvider/setup")
                .get(req -> Results.with(internalsController().getStatisticsProviderSetup()));
        use("/fuse/internal/statisticsProvider/refresh")
                .put(req -> Results.with(internalsController().refreshStatisticsProviderSetup()));
    }

    private void registerIdGenerator(AppUrlSupplier localUrlSupplier) {
        use("/fuse/idgen/:id").get(req -> idGeneratorCtrl().getNext(req.param("id").value(), req.param("numIds").intValue()));
    }

    private void registerDataLoaderApi(AppUrlSupplier localUrlSupplier) {
        /** get the health status of the service */
        use("/fuse/catalog/ontology/:id/init")
                .get(req -> Results.with(dataLoaderCtrl().init(req.param("id").value())));

        use("/fuse/catalog/ontology/:id/load")
                .get(req -> Results.with(dataLoaderCtrl().load(req.param("id").value())));

        use("/fuse/catalog/ontology/:id/drop")
                .get(req -> Results.with(dataLoaderCtrl().drop(req.param("id").value())));
    }

    private void registerCatalogApi(AppUrlSupplier localUrlSupplier) {
        /** get available ontologies*/
        use("/fuse/catalog/ontology")
                .get(req -> {
                    List<ContentResponse<Ontology>> responses = catalogCtrl().getOntologies();
                    return Results.with(responses, !responses.isEmpty() ? responses.get(0).status() : Status.NO_CONTENT);
                });

        /** get the ontology by id */
        use("/fuse/catalog/ontology/:id")
                .get(req -> {
                    ContentResponse response = catalogCtrl().getOntology(req.param("id").value());
                    return Results.with(response, response.status());
                });

        /** get available schemas **/
        use("/fuse/catalog/schema")
                .get(req -> {
                    List<ContentResponse> responses = catalogCtrl().getSchemas();
                    return Results.with(responses, !responses.isEmpty() ? responses.get(0).status() : Status.NO_CONTENT);
                });

        use("/fuse/catalog/schema/:id")
                .get(req -> {
                    ContentResponse response = catalogCtrl().getSchema(req.param("id").value());
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
                    CreateQueryRequest createQueryRequest = req.body(CreateQueryRequest.class);
                    req.set(CreateQueryRequest.class, createQueryRequest);
                    req.set(PlanTraceOptions.class, createQueryRequest.getPlanTraceOptions());
                    ContentResponse<QueryResourceInfo> response = queryCtrl().create(createQueryRequest);

                    return Results.with(response, response.status());
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

        /** get the query verbose plan */
        use(localUrlSupplier.resourceUrl(":queryId") + "/planVerbose")
                .get(req -> {
                    ContentResponse response = queryCtrl().planVerbose(req.param("queryId").value());
                    //temporary fix for json serialization of object graphs
                    return Results.with(new ObjectMapper().writeValueAsString(response.getData()), response.status())
                            .type("application/json");
                });
        /** get the print of the execution plan */
        use(localUrlSupplier.resourceUrl(":queryId") + "/plan/print")
                .get(req -> {
                    ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> response = queryCtrl().explain(req.param("queryId").value());
                    String print = PlanWithCostDescriptor.print(response.getData());
                    ContentResponse<String> compose = Builder.<String>builder(randomUUID().toString(), OK, NOT_FOUND)
                            .data(Optional.of(print))
                            .compose();
                    return Results.with(JsonWriter.objectToJson(compose), response.status());
                });

        /** get the query v1*/
        use(localUrlSupplier.resourceUrl(":queryId") + "/v1")
                .get(req -> {
                    ContentResponse response = queryCtrl().getV1(req.param("queryId").value());
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });
        /** get the query v1 print*/
        use(localUrlSupplier.resourceUrl(":queryId") + "/v1/print")
                .get(req -> {
                    ContentResponse<Query> response = queryCtrl().getV1(req.param("queryId").value());
                    String print = QueryDescriptor.print(response.getData());
                    ContentResponse<String> compose = Builder.<String>builder(randomUUID().toString(), OK, NOT_FOUND)
                            .data(Optional.of(print))
                            .compose();
                    return Results.with(JsonWriter.objectToJson(compose), response.status());
                });

        /** get the asg query */
        use(localUrlSupplier.resourceUrl(":queryId") + "/asg")
                .get(req -> {
                    ContentResponse<AsgQuery> response = queryCtrl().getAsg(req.param("queryId").value());
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });

        /** get the asg query print*/
        use(localUrlSupplier.resourceUrl(":queryId") + "/asg/print")
                .get(req -> {
                    ContentResponse<AsgQuery> response = queryCtrl().getAsg(req.param("queryId").value());
                    String print = AsgQueryDescriptor.print(response.getData());
                    ContentResponse<String> compose = Builder.<String>builder(randomUUID().toString(), OK, NOT_FOUND)
                            .data(Optional.of(print))
                            .compose();
                    return Results.with(JsonWriter.objectToJson(compose), response.status());
                });

        /** get the query plan execution */
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
