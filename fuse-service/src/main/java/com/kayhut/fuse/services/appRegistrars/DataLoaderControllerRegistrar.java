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

import com.fasterxml.jackson.databind.JsonNode;
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
        app.get("/fuse/catalog/ontology/:id/init",
                req -> Results.with(this.getController(app).init(req.param("id").value())));

        app.post("/fuse/catalog/ontology/:id/load",
                req -> Results.with(this.getController(app).load(req.param("id").value(), req.body(JsonNode.class))));

        app.get("/fuse/catalog/ontology/:id/drop",
                req -> Results.with(this.getController(app).drop(req.param("id").value())));
    }
    //endregion
}
