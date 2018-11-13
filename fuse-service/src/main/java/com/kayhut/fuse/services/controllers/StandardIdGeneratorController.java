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
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.CREATED;
import static org.jooby.Status.SERVER_ERROR;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class StandardIdGeneratorController<TId> implements IdGeneratorController<TId> {
    //region Constructors
    @Inject
    public StandardIdGeneratorController(IdGeneratorDriver<TId> driver) {
        this.driver = driver;
    }
    //endregion

    //region IdGenerator Implementation
    @Override
    public ContentResponse<TId> getNext(String genName, int numIds) {
        return ContentResponse.Builder.<TId>builder(CREATED, SERVER_ERROR )
                .data(Optional.of(this.driver.getNext(genName, numIds)))
                .compose();
    }
    //endregion

    //region Fields
    private IdGeneratorDriver<TId> driver;
    //endregion
}
