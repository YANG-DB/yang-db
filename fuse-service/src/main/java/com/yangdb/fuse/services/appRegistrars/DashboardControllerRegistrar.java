package com.yangdb.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.services.controllers.DashboardController;
import org.jooby.Jooby;
import org.jooby.Results;

public class DashboardControllerRegistrar extends AppControllerRegistrarBase<DashboardController> {
    //region Constructors
    public DashboardControllerRegistrar() {
        super(DashboardController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/fuse/dashboard/entities",
                req -> Results.with(this.getController(app).graphElementCount()));
        app.get("/fuse/dashboard/fields",
                req -> Results.with(this.getController(app).graphFieldValuesCount()));
        app.get("/fuse/dashboard/created",
                req -> Results.with(this.getController(app).graphElementCreatedOverTime()));
        app.get("/fuse/dashboard/count",
                req -> Results.with(this.getController(app).cursorCount()));
    }
    //endregion
}
