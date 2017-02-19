package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.events.ExecutionCompleteEvent;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.Path;
import com.kayhut.fuse.model.Plan;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;

import java.util.UUID;

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
        Graph graph = Graph.GraphBuilder.builder(id).data("Simple Graph Data").url("/result").compose();
        return new Response(id, request.getName(), graph);
    }
}
