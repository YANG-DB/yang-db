package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.services.controllers.DataLoaderController;
import org.jooby.Jooby;
import org.jooby.Results;

public class DataLoaderControllerRegistrar extends AppControllerRegistrarBase<DataLoaderController> {
    //region Constructors
    public DataLoaderControllerRegistrar() {
        super(DataLoaderController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */
        app.use("/fuse/catalog/ontology/:id/init")
                .get(req -> Results.with(this.getController(app).init(req.param("id").value())));

        app.use("/fuse/catalog/ontology/:id/load")
                .get(req -> Results.with(this.getController(app).load(req.param("id").value())));

        app.use("/fuse/catalog/ontology/:id/drop")
                .get(req -> Results.with(this.getController(app).drop(req.param("id").value())));
    }
    //endregion
}
