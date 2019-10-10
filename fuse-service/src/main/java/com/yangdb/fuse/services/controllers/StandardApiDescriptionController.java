package com.yangdb.fuse.services.controllers;

/*-
 *
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;

import java.util.Optional;

import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

/**
 * Created by Roman on 11/06/2017.
 */
public class StandardApiDescriptionController implements ApiDescriptionController {
    //region Constructors
    @Inject
    public StandardApiDescriptionController(AppUrlSupplier urlSupplier) {
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        return ContentResponse.Builder.<FuseResourceInfo>builder(OK, NOT_FOUND)
                .data(Optional.of(new FuseResourceInfo(
                        "/fuse",
                        "/fuse/internal",
                        "/fuse/health",
                        this.urlSupplier.queryStoreUrl(),
                        "/fuse/search", this.urlSupplier.catalogStoreUrl())))
                .compose();
    }
    //endregion

    //region Fields
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
