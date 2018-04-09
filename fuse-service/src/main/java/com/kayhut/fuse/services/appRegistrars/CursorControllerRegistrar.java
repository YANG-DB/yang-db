package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.CursorController;
import org.jooby.Jooby;
import org.jooby.Results;

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
                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value());
                    return Results.with(response, response.status());
                });

        /** create a query cursor */
        app.use(appUrlSupplier.cursorStoreUrl(":queryId"))
                .post(req -> {
                    ContentResponse response = this.getController(app).create(req.param("queryId").value(), req.body(CreateCursorRequest.class));
                    return Results.with(response, response.status());
                });

        /** get the cursor resource info */
        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId"))
                .get(req -> {
                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        app.use(appUrlSupplier.resourceUrl(":queryId", ":cursorId"))
                .delete(req -> {
                    ContentResponse response = this.getController(app).delete(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });
    }
    //endregion
}
