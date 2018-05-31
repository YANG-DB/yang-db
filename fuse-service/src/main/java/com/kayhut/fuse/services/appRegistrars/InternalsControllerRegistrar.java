package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.services.controllers.InternalsController;
import org.jooby.Jooby;
import org.jooby.Results;

public class InternalsControllerRegistrar extends AppControllerRegistrarBase<InternalsController> {
    //region Constructors
    public InternalsControllerRegistrar() {
        super(InternalsController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */
        app.use("/fuse/internal/statisticsProvider/name")
                .get(req -> Results.with(this.getController(app).getStatisticsProviderName()));
        app.use("/fuse/internal/version")
                .get(req -> Results.with(this.getController(app).getVersion()));
        app.use("/fuse/internal/statisticsProvider/setup")
                .get(req -> Results.with(this.getController(app).getStatisticsProviderSetup()));
        app.use("/fuse/internal/statisticsProvider/refresh")
                .put(req -> Results.with(this.getController(app).refreshStatisticsProviderSetup()));
    }
    //endregion
}
