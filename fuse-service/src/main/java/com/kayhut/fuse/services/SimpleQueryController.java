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

import static com.kayhut.fuse.model.Utils.getOrCreateId;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleQueryController implements QueryController {
    private EventBus eventBus;

    @Inject
    public SimpleQueryController(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Response graphQuery(Request request) {
        //build graph & transport response
        String id = getOrCreateId(request.getId());
        Graph graph = Graph.GraphBuilder.builder(id).data("Simple Graph Data").url("/result").compose();
        Response response = new Response(graph.getId(), request.getName(), graph);
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteEvent(request,response.getContent()));
        return response;
    }

    @Override
    public Response pathQuery(Request request) {
        //build path & transport response
        String id = getOrCreateId(request.getId());
        Path path = Path.PathBuilder.builder(id).data("Simple Path Data").url("/result").compose();
        Response response = new Response(path.getId(), request.getName(), path);
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteEvent(request,response.getContent()));
        return response;
    }

    @Override
    public Response plan(Request request) {
        //build plan & transport response
        String id = getOrCreateId(request.getId());
        Plan plan = Plan.PlanBuilder.builder(id).data("Simple Plan").compose();
        Response response = new Response(plan.getId(), request.getName(), plan);
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteEvent(request,response.getContent()));
        return response;
    }
}
