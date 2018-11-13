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
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardCursorController implements CursorController {
    //region Constructors
    @Inject
    public StandardCursorController(CursorDriver driver) {
        this.driver = driver;
    }
    //endregion

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        return Builder.<CursorResourceInfo>builder(CREATED, SERVER_ERROR)
                .data(this.driver.create(queryId, createCursorRequest))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        return Builder.<StoreResourceInfo>builder(OK, NOT_FOUND)
                .data(this.driver.getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        return Builder.<CursorResourceInfo>builder(OK, NOT_FOUND)
                .data(this.driver.getInfo(queryId, cursorId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        return Builder.<Boolean>builder(ACCEPTED, NOT_FOUND)
                .data(this.driver.delete(queryId, cursorId)).compose();
    }
    //endregion

    //region Fields
    private CursorDriver driver;
    //endregion
}
