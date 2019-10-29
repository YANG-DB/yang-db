package com.yangdb.fuse.services.controllers.logging;

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

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.driver.CursorDriver;
import com.yangdb.fuse.dispatcher.logging.*;
import com.yangdb.fuse.dispatcher.logging.LogMessage.MDCWriter.Composite;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.services.controllers.CursorController;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.info;
import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.yangdb.fuse.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCursorController extends LoggingControllerBase<CursorController> implements CursorController<CursorController,CursorDriver> {
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

    @Override
    public CursorController driver(CursorDriver driver) {
        return (CursorController) this.controller.driver(driver);
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter getInfoByQueryId = MethodName.of("getInfoByQueryId");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorId = MethodName.of("getInfoByQueryIdAndCursorId");
    private static MethodName.MDCWriter delete = MethodName.of("delete");

    //endregion
}
