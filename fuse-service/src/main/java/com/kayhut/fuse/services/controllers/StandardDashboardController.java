package com.kayhut.fuse.services.controllers;

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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.driver.DashboardDriver;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.kayhut.fuse.services.suppliers.CachedRequestIdSupplier.RequestIdSupplierParameter;
import static org.jooby.Status.ACCEPTED;
import static org.jooby.Status.NOT_FOUND;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardDashboardController implements DashboardController {
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
    public ContentResponse<Map> graphElementCount() {
        return Builder.<Map>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver.graphElementCount()))
                .compose();
    }

    @Override
    public ContentResponse<Map> graphElementCreatedOverTime() {
        return Builder.<Map>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver.graphElementCreated()))
                .compose();
    }

    @Override
    public ContentResponse<Map> graphFieldValuesCount() {
        return Builder.<Map>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver.graphFieldValuesCount()))
                .compose();
    }

    @Override
    public ContentResponse<Map> cursorCount() {
        return Builder.<Map>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver.cursorCount()))
                .compose();
    }

    //endregion

    //region Fields
    private DashboardDriver driver;
    private RequestIdSupplier requestIdSupplier;
    private Set<CompositeCursorFactory.Binding> cursorBindings;
    //endregion
}
