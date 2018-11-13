package com.kayhut.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
        app.use("/fuse/internal/snowflakeId")
                .get(req -> Results.with(this.getController(app).getSnowflakeId()));
        app.use("/fuse/internal/cursorBindings")
                .get(req -> Results.with(this.getController(app).getCursorBindings()));
        app.use("/fuse/internal/statisticsProvider/setup")
                .get(req -> Results.with(this.getController(app).getStatisticsProviderSetup()));
        app.use("/fuse/internal/statisticsProvider/refresh")
                .put(req -> Results.with(this.getController(app).refreshStatisticsProviderSetup()));
    }
    //endregion
}
