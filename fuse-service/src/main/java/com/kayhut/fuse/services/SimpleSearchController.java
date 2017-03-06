package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.transport.GraphContent;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.process.QueryResourceResult;
import com.kayhut.fuse.model.transport.QueryRequest;
import com.kayhut.fuse.model.transport.ContentResponse;

import static com.kayhut.fuse.model.Utils.getOrCreateId;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleSearchController implements SearchController {
    private EventBus eventBus;

    @Inject
    public SimpleSearchController(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public ContentResponse search(QueryRequest request) {
        /*String id = getOrCreateId(request.getId());
        ContentResponse response = ContentResponse.ResponseBuilder.builder(id)
                .queryMetadata(new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis()))
                //todo implement this
                .queryResourceResult(new QueryResourceResult())
                .data(GraphContent.GraphBuilder.builder(request.getId())
                        .data(new QueryResult())
                        .compose())
                .compose();
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteCommand(response));
        return response;*/
        return null;
    }
}
