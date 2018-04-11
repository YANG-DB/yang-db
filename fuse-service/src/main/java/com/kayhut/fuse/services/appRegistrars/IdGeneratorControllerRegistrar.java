package com.kayhut.fuse.services.appRegistrars;

import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.services.controllers.IdGeneratorController;
import org.jooby.Jooby;

public class IdGeneratorControllerRegistrar extends AppControllerRegistrarBase<IdGeneratorController<Object>> {
    //region Constructors
    public IdGeneratorControllerRegistrar() {
        super(new TypeLiteral<IdGeneratorController<Object>>(){});
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.use("/fuse/idgen/:id").get(req -> this.getController(app).getNext(req.param("id").value(), req.param("numIds").intValue()));
    }
    //endregion
}
