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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.transport.ContentResponse;
import org.opensearch.client.Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jooby.Status.CREATED;
import static org.jooby.Status.SERVER_ERROR;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class StandardIdGeneratorController<TId> implements IdGeneratorController<TId> {
    //region Constructors
    @Inject
    public StandardIdGeneratorController(IdGeneratorDriver<TId> driver ) {
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

    @Override
    public ContentResponse<Boolean> init(List<String> names) {
        return ContentResponse.Builder.<Boolean>builder(CREATED, SERVER_ERROR )
                .data(Optional.of(this.driver.init(names)))
                .compose();
    }
    //endregion

    //region Fields
    private IdGeneratorDriver<TId> driver;
    //endregion
}
