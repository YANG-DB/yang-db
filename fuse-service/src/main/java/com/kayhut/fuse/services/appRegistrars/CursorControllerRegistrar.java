package com.kayhut.fuse.services.appRegistrars;

import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.logging.Route;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ExecutionScope;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.CursorController;
import javaslang.collection.Stream;
import org.jooby.Jooby;
import org.jooby.Results;

import java.util.Optional;
import java.util.Set;

public class CursorControllerRegistrar extends AppControllerRegistrarBase<CursorController> {
    //region Constructors
    public CursorControllerRegistrar() {
        super(CursorController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the query cursor store info */
        app.use(appUrlSupplier.cursorStoreUrl(":queryId"))
                .get(req -> {
                    Route.of("getCursorStore").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** create a query cursor */
        app.use(appUrlSupplier.cursorStoreUrl(":queryId"))
                .post(req -> {
                    Route.of("postCursor").write();
                    CreateCursorRequest cursorRequest = req.body(CreateCursorRequest.class);

                    req.set(ExecutionScope.class, new ExecutionScope(1000 * 60 * 10));
                    ContentResponse response = this.getController(app).create(req.param("queryId").value(), cursorRequest);

                    return Results.with(response, response.status());
                });

        /** get the cursor resource info */
        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId"))
                .get(req -> {
                    Route.of("getCursor").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId"))
                .delete(req -> {
                    Route.of("deleteCursor").write();

                    ContentResponse response = this.getController(app).delete(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });
    }
    //endregion
}
