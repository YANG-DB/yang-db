package com.kayhut.fuse.services;

/*-
 * #%L
 * fuse-domain-knowledge-poc
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
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.kayhut.fuse.services.controller.PocGraphController;
import org.jooby.Jooby;
import org.jooby.Results;

public class PocControllerRegistrar extends AppControllerRegistrarBase<PocGraphController> {

    public static final String BASE = "/poc/graph";

    //region Constructors
    public PocControllerRegistrar() {
        super(PocGraphController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */

        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/rank").resourceUrl(":queryId", ":cursorId", ":pageId"),
                req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("queryId").value(),
                        req.param("cursorId").value(),
                        req.param("pageId").value(),
                        req.param("context").isSet() ? req.param("context").value() : null)));

        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/rank/report").baseUrl(),
                req -> Results.json(this.getController(app).getGraphWithRankReport(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null,
                        req.param("category").isSet() ? req.param("category").value() : null,
                        req.param("headers").isSet() ? req.param("headers").value().split(",") : new String[]{"title"})));

        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/rank").baseUrl(),
                req -> Results.json(this.getController(app).getGraphWithRank(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null,
                        req.param("category").isSet() ? req.param("category").value() : null)));


        app.get(new DefaultAppUrlSupplier(appUrlSupplier.baseUrl() + BASE + "/connectedComp").baseUrl(),
                req -> Results.json(this.getController(app).getGraphWithConnectedComponents(
                        req.param("cache").isSet() ? req.param("cache").booleanValue() : false,
                        req.param("count").isSet() ? req.param("count").intValue() : -1,
                        req.param("context").isSet() ? req.param("context").value() : null)));

    }
    //endregion
}
