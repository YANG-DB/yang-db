package com.kayhut.fuse.services.controllers;

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

import com.cedarsoftware.util.io.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.driver.InternalsDriver;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.services.suppliers.SnowflakeRequestIdSupplier;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.kayhut.fuse.services.suppliers.CachedRequestIdSupplier.RequestIdSupplierParameter;
import static org.jooby.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardInternalsController implements InternalsController<StandardInternalsController,InternalsDriver> {
    //region Constructors
    @Inject
    public StandardInternalsController(
            InternalsDriver driver,
            @Named(RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier,
            Set<CompositeCursorFactory.Binding> cursorBindings) {
        this.driver = driver;
        this.requestIdSupplier = requestIdSupplier;
        this.cursorBindings = cursorBindings;
    }
    //endregion

    //region InternalsController
    @Override
    public ContentResponse<String> getVersion() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(String.format("%d_%d",1900+(new Date().getYear()) , new Date().getMonth())))
                .compose();
    }

    @Override
    public ContentResponse<Long> getSnowflakeId() {
        return Builder.<Long>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(((SnowflakeRequestIdSupplier) requestIdSupplier)
                        .getWorkerId())).compose();
    }

    @Override
    public ContentResponse<Map<String, Class<? extends CreateCursorRequest>>> getCursorBindings() {
        return Builder.<Map<String, Class<? extends CreateCursorRequest>>>builder(OK, NOT_FOUND)
                .data(Optional.of(
                        Stream.ofAll(this.cursorBindings).toJavaMap(binding -> new Tuple2<>(binding.getType(), binding.getKlass()))))
                .compose();
    }

    @Override
    public ContentResponse<String> getStatisticsProviderName() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(driver().getStatisticsProviderName()).compose();
    }

    @Override
    public ContentResponse<Map> getConfig() {
        return Builder.<Map>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver().getConfig().get().root().unwrapped()))
                .compose();

    }

    @Override
    public ContentResponse<String> getStatisticsProviderSetup() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(driver().getStatisticsProviderSetup()).compose();
    }

    @Override
    public ContentResponse<String> refreshStatisticsProviderSetup() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(driver().refreshStatisticsProviderSetup()).compose();
    }

    protected InternalsDriver driver() {
        return driver;
    }

    @Override
    public StandardInternalsController driver(InternalsDriver driver) {
        this.driver = driver;
        return this;
    }
    //endregion

    //region Fields
    private InternalsDriver driver;
    private RequestIdSupplier requestIdSupplier;
    private Set<CompositeCursorFactory.Binding> cursorBindings;

    //endregion
}
