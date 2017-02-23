package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.command.ExecutionCompleteCommand;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.results.ResultMetadata;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;

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
    public Response search(Request request) {
        String id = getOrCreateId(request.getId());
        Response response = Response.ResponseBuilder.builder(id)
                .queryMetadata(new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis()))
                //todo implement this
                .resultMetadata(new ResultMetadata())
                .data(Graph.GraphBuilder.builder(request.getId())
                        .data(new QueryResult())
                        .compose())
                .compose();
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteCommand(response));
        return response;
    }
}
