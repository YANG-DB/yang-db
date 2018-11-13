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
import com.google.inject.Singleton;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardSearchController implements SearchController {
    //region Constructors
    @Inject
    public StandardSearchController() {

    }
    //endregion

    //region SearchController Implementation
    @Override
    public ContentResponse search(CreateQueryRequest request) {
        /*String id = getOrCreateId(request.getRequestId());
        ContentResponse response = ContentResponse.Builder.builder(id)
                .queryMetadata(new QueryMetadata(id, request.getType(), request.getType(), System.currentTimeMillis()))
                //todo implement this
                .queryResourceResult(new QueryResourceInfo())
                .data(GraphContent.GraphBuilder.builder(request.getRequestId())
                        .data(new AssignmentsQueryResult())
                        .build())
                .build();
        return response;*/
        return null;
    }
    //endregion

    //region Fields
    //endregion
}
