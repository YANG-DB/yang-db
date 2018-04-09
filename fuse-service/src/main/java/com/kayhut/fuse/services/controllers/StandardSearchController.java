package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

/**
 * Created by lior on 19/02/2017.
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
        /*String id = getOrCreateId(request.getId());
        ContentResponse response = ContentResponse.Builder.builder(id)
                .queryMetadata(new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis()))
                //todo implement this
                .queryResourceResult(new QueryResourceInfo())
                .data(GraphContent.GraphBuilder.builder(request.getId())
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
