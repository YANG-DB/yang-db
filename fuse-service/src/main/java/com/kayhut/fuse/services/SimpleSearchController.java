package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.events.ExecutionCompleteEvent;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.process.QueryMetadata;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;

import static com.kayhut.fuse.model.Utils.getOrCreateId;
import static com.kayhut.fuse.model.Utils.readJsonFile;

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
                .metadata(new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis()))
                .data(Graph.GraphBuilder.builder(request.getId())
                        .data(readJsonFile("result.json"))
                        .url("/result")
                        .compose())
                .compose();
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteEvent(request, response));
        return response;
    }
}
