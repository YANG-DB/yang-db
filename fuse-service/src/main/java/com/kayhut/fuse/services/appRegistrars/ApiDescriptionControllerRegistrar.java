package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.services.controllers.ApiDescriptionController;
import org.jooby.Jooby;

public class ApiDescriptionControllerRegistrar extends AppControllerRegistrarBase<ApiDescriptionController> {
    //region Constructors
    public ApiDescriptionControllerRegistrar() {
        super(ApiDescriptionController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.use("/fuse").get(() -> this.getController(app).getInfo());
    }
    //endregion
}
