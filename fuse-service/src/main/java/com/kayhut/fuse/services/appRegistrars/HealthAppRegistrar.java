package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import org.jooby.Jooby;

public class HealthAppRegistrar implements AppRegistrar {
    //region AppRegistrar Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */
        app.use("/fuse/health")
                /** check health */
                .get(() -> "Alive And Well...");
    }
    //endregion
}
