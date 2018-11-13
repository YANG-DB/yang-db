package com.kayhut.fuse.services.controllers.logging;

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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import com.kayhut.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.CursorController;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCursorController extends LoggingControllerBase<CursorController> implements CursorController {
    public static final String controllerParameter = "LoggingCursorController.@controller";
    public static final String loggerParameter = "LoggingCursorController.@logger";

    //region Constructors
    @Inject
    public LoggingCursorController(
            @Named(controllerParameter) CursorController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        return new LoggingSyncMethodDecorator<ContentResponse<CursorResourceInfo>>(
                this.logger,
                this.metricRegistry,
                create,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.create(queryId, createCursorRequest), this.resultHandler());
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<StoreResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfoByQueryId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        return new LoggingSyncMethodDecorator<ContentResponse<CursorResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfoByQueryIdAndCursorId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(queryId, cursorId), this.resultHandler());
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Boolean>>(
                this.logger,
                this.metricRegistry,
                delete,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.delete(queryId, cursorId), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter getInfoByQueryId = MethodName.of("getInfoByQueryId");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorId = MethodName.of("getInfoByQueryIdAndCursorId");
    private static MethodName.MDCWriter delete = MethodName.of("delete");
    //endregion
}
