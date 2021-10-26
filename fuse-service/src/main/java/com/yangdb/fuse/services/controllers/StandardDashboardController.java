package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.driver.DashboardDriver;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.ContentResponse.Builder;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;

import java.util.Optional;
import java.util.Set;

import static com.yangdb.fuse.services.suppliers.CachedRequestIdSupplier.RequestIdSupplierParameter;
import static com.yangdb.fuse.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardDashboardController implements DashboardController<StandardDashboardController,DashboardDriver> {
    //region Constructors
    @Inject
    public StandardDashboardController(
            DashboardDriver driver,
            @Named(RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier,
            Set<CompositeCursorFactory.Binding> cursorBindings) {
        this.driver = driver;
        this.requestIdSupplier = requestIdSupplier;
        this.cursorBindings = cursorBindings;
    }
    //endregion

    @Override
    public ContentResponse<ObjectNode> graphElementCount(String ontology) {
        return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver().graphElementCount(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<ObjectNode> graphElementCreatedOverTime(String ontology) {
        return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver().graphElementCreated(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<ObjectNode> cursorCount() {
        return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver().cursorCount()))
                .compose();
    }

    protected DashboardDriver driver() {
        return driver;
    }

    //endregion

    @Override
    public StandardDashboardController driver(DashboardDriver driver) {
        this.driver = driver;
        return this;
    }

    //region Fields
    private DashboardDriver driver;
    private RequestIdSupplier requestIdSupplier;
    private Set<CompositeCursorFactory.Binding> cursorBindings;

    //endregion
}
