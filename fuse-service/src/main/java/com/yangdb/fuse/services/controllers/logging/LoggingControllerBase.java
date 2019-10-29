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
import com.yangdb.fuse.dispatcher.decorators.MethodDecorator;
import com.yangdb.fuse.dispatcher.logging.Elapsed;
import com.yangdb.fuse.dispatcher.logging.LogMessage;
import com.yangdb.fuse.logging.RequestExternalMetadata;
import com.yangdb.fuse.logging.RequestId;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.suppliers.RequestExternalMetadataSupplier;
import com.yangdb.fuse.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

public abstract class LoggingControllerBase<TController> {
    //region Constructors
    public LoggingControllerBase(
            TController controller,
            Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        this.controller = controller;
        this.logger = logger;
        this.requestIdSupplier = requestIdSupplier;
        this.requestExternalMetadataSupplier = requestExternalMetadataSupplier;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region Protected Methods
    protected LogMessage.MDCWriter primerMdcWriter() {
        return LogMessage.MDCWriter.Composite.of(
                Elapsed.now(),
                RequestId.of(this.requestIdSupplier.get()),
                RequestExternalMetadata.of(this.requestExternalMetadataSupplier.get()));
    }

    protected <TResult> MethodDecorator.ResultHandler<ContentResponse<TResult>, Long> resultHandler() {
        return new MethodDecorator.ResultHandler<ContentResponse<TResult>, Long>() {
            @Override
            public ContentResponse<TResult> onSuccess(ContentResponse<TResult> response, Long elapsed) {
                return ContentResponse.Builder.builder(response)
                        .requestId(requestIdSupplier.get())
                        .external(requestExternalMetadataSupplier.get())
                        .elapsed(elapsed)
                        .compose();
            }

            @Override
            public ContentResponse<TResult> onFailure(Exception ex, Long elapsed) {
                return onSuccess(ContentResponse.internalError(ex), elapsed);
            }
        };
    }
    //endregion

    //region Fields
    protected TController controller;
    protected Logger logger;
    protected RequestIdSupplier requestIdSupplier;
    protected RequestExternalMetadataSupplier requestExternalMetadataSupplier;
    protected MetricRegistry metricRegistry;
    //endregion
}
