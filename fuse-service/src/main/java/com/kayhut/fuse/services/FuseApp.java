package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.process.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
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
        registerFuseApi(urlSupplier);
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

    private void registerFuseApi(AppUrlSupplier urlSupplier) {
        use("/fuse")
                .get(() -> new FuseResourceInfo(
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
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.FOUND);
                });
    }

    private void registerQueryApi(AppUrlSupplier urlSupplier) {
        /** create a query */
        use(urlSupplier.queryStoreUrl())
                .post(req -> Results.with(queryCtrl().create(req.body(CreateQueryRequest.class)), Status.CREATED));

        /** get the query info */
        use(urlSupplier.resourceUrl(":queryId"))
                .get(req -> {
                    ContentResponse response = queryCtrl().getInfo(req.param("queryId").value());
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.FOUND);
                });

        /** delete a query */
        use(urlSupplier.resourceUrl(":queryId"))
                .delete(req -> {
                    ContentResponse response = queryCtrl().delete(req.param("queryId").value());
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.OK);
                });

        /** get the query execution plan */
        use(urlSupplier.resourceUrl(":queryId") + "/plan")
                .get(req -> {
                    ContentResponse response = queryCtrl().explain(req.param("queryId").value());
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.CREATED);
                });
    }

    private void registerCursorApi(AppUrlSupplier urlSupplier) {
        /** create a query cursor */
        use(urlSupplier.cursorStoreUrl(":queryId"))
                .post(req -> {
                    ContentResponse response = cursorCtrl().create(req.param("queryId").value(), req.body(CreateCursorRequest.class));
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.CREATED);
                });

        /** get the cursor resource info */
        use(urlSupplier.resourceUrl(":queryId", ":cursorId"))
                .get(req -> {
                    ContentResponse response = cursorCtrl().getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.FOUND);
                });

        use(urlSupplier.resourceUrl(":queryId", ":cursorId"))
                .delete(req -> {
                    ContentResponse response = cursorCtrl().delete(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.OK);
                });
    }

    private void registerPageApi(AppUrlSupplier urlSupplier) {
        /** create the next page */
        use(urlSupplier.pageStoreUrl(":queryId", ":cursorId"))
                .post(req -> Results.with(pageCtrl().create(req.param("queryId").value(), req.param("cursorId").value(), req.body(CreatePageRequest.class)), Status.CREATED));

        /** get page by id */
        use(urlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"))
                .get(req -> {
                    ContentResponse response = pageCtrl().get(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.FOUND);
                });

        /** delete page by id */
        use(urlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"))
                .delete(req -> {
                    ContentResponse response = pageCtrl().delete(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response == ContentResponse.NOT_FOUND ? Status.NOT_FOUND : Status.OK);
                });
    }

    private void registerSearchApi(AppUrlSupplier urlSupplier) {
        /** submit a search */
        use("/fuse/search")
                .post(req -> Results.with(searchCtrl().search(req.body(CreateQueryRequest.class)), Status.CREATED));
    }
    //endregion

    public static void main(final String[] args) {
        run(() -> new FuseApp(new DefaultAppUrlSupplier("/fuse")), args);
    }
}
