package com.kayhut.fuse.services;

import com.cedarsoftware.util.io.JsonWriter;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.json.Jackson;
import org.jooby.scanner.Scanner;


@SuppressWarnings({"unchecked", "rawtypes"})
public class FuseApp extends Jooby {
    //region Consructors
    public FuseApp(AppUrlSupplier urlSupplier) {
        use(new Scanner());
        use(new Jackson());

        registerCors(urlSupplier);
        registerFuseApiDescription(urlSupplier);
        registerHealthApi(urlSupplier);
        registerCatalogApi(urlSupplier);
        registerQueryApi(urlSupplier);
        registerCursorApi(urlSupplier);
        registerPageApi(urlSupplier);
        registerSearchApi(urlSupplier);
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
    private void registerCors(AppUrlSupplier urlSupplier) {
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

    private void registerFuseApiDescription(AppUrlSupplier urlSupplier) {
        use("/fuse")
                .get(() -> new FuseResourceInfo(
                        "/fuse",
                        "/fuse/health",
                        urlSupplier.queryStoreUrl(),
                        "/fuse/search",
                        "/fuse/catalog"));
    }

    private void registerHealthApi(AppUrlSupplier urlSupplier) {
        /** get the health status of the service */
        use("/fuse/health")
                /** check health */
                .get(() -> "Alive And Well...");
    }

    private void registerCatalogApi(AppUrlSupplier urlSupplier) {
        /** get the ontology by id */
        use("/fuse/catalog/ontology/:id")
                /** check health */
                .get(req -> {
                    ContentResponse response = catalogCtrl().get(req.param("id").value());
                    return Results.with(response, response.status());
                });
    }

    private void registerQueryApi(AppUrlSupplier urlSupplier) {
        /** get the query store info */
        use(urlSupplier.queryStoreUrl())
                .get(req -> Results.with(queryCtrl().getInfo(), Status.FOUND));

        /** create a query */
        use(urlSupplier.queryStoreUrl())
                .post(req -> {
                    ContentResponse<QueryResourceInfo> entity = queryCtrl().create(req.body(CreateQueryRequest.class));
                    return Results.with(entity, entity.status());
                });

        /** get the query info */
        use(urlSupplier.resourceUrl(":queryId"))
                .get(req -> {
                    ContentResponse response = queryCtrl().getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** delete a query */
        use(urlSupplier.resourceUrl(":queryId"))
                .delete(req -> {
                    ContentResponse response = queryCtrl().delete(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** get the query execution plan */
        use(urlSupplier.resourceUrl(":queryId") + "/plan")
                .get(req -> {
                    ContentResponse response = queryCtrl().explain(req.param("queryId").value());
                    //temporary fix for jason serialization of object graphs
                    return Results.with(JsonWriter.objectToJson(response), response.status());
                });
    }

    private void registerCursorApi(AppUrlSupplier urlSupplier) {
        /** get the query cursor store info */
        use(urlSupplier.cursorStoreUrl(":queryId"))
                .get(req -> {
                    ContentResponse response = cursorCtrl().getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** create a query cursor */
        use(urlSupplier.cursorStoreUrl(":queryId"))
                .post(req -> {
                    ContentResponse response = cursorCtrl().create(req.param("queryId").value(), req.body(CreateCursorRequest.class));
                    return Results.with(response, response.status());
                });

        /** get the cursor resource info */
        use(urlSupplier.resourceUrl(":queryId", ":cursorId"))
                .get(req -> {
                    ContentResponse response = cursorCtrl().getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        use(urlSupplier.resourceUrl(":queryId", ":cursorId"))
                .delete(req -> {
                    ContentResponse response = cursorCtrl().delete(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });
    }

    private void registerPageApi(AppUrlSupplier urlSupplier) {
        /** get the cursor page store info*/
        use(urlSupplier.pageStoreUrl(":queryId", ":cursorId"))
                .get(req -> {
                    ContentResponse response = pageCtrl().getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        /** create the next page */
        use(urlSupplier.pageStoreUrl(":queryId", ":cursorId"))
                .post(req -> {
                    ContentResponse<PageResourceInfo> entity = pageCtrl().create(req.param("queryId").value(), req.param("cursorId").value(), req.body(CreatePageRequest.class));
                    return Results.with(entity, entity.status());
                });

        /** get page info by id */
        use(urlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"))
                .get(req -> {
                    ContentResponse response = pageCtrl().getInfo(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by id */
        use(urlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/data")
                .get(req -> {
                    ContentResponse response = pageCtrl().getData(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });
    }

    private void registerSearchApi(AppUrlSupplier urlSupplier) {
        /** submit a search */
        use("/fuse/search")
                .post(req -> {
                    ContentResponse search = searchCtrl().search(req.body(CreateQueryRequest.class));
                    return Results.with(search, search.status());
                });
    }
    //endregion

    public static void main(final String[] args) {
        run(() -> new FuseApp(new DefaultAppUrlSupplier("/fuse")), args);
    }
}
