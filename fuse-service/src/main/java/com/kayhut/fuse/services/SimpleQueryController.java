package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.DispatcherDriver;
import com.kayhut.fuse.events.ExecutionCompleteEvent;
import com.kayhut.fuse.model.Graph;
import com.kayhut.fuse.model.Path;
import com.kayhut.fuse.model.Plan;
import com.kayhut.fuse.model.Utils;
import com.kayhut.fuse.model.process.ProcessElement;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.process.QueryMetadata;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.Response;

import java.util.UUID;

import static com.kayhut.fuse.model.Utils.getOrCreateId;
import static com.kayhut.fuse.model.process.ProcessElement.ProcessContext.get;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleQueryController implements QueryController {

    private EventBus eventBus;
    private DispatcherDriver driver;

    @Inject
    public SimpleQueryController(EventBus eventBus, DispatcherDriver driver) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.driver = driver;
    }

    @Override
    public Response graphQuery(Request request) {
        //build graph & transport response
        String id = getOrCreateId(request.getId());
        QueryMetadata metadata = new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis());
        driver.process(new QueryData(metadata));
        // Get the response from context - actually the event bus is doing sync (serial) method (listeners) invocation until the last element in the execution chain is called
        // and sets the response in the context (DispatcherDriver.response)
        Response response = get();
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteEvent(request, response));
        return response;
    }

    @Override
    public Response pathQuery(Request request) {
        //build path & transport response
        String id = getOrCreateId(request.getId());
        QueryMetadata metadata = new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis());
        driver.process(new QueryData(metadata));
        // Get the response from context - actually the event bus is doing sync (serial) method (listeners) invocation until the last element in the execution chain is called
        // and sets the response in the context (DispatcherDriver.response)
        Response response = get();
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteEvent(request, response));
        return response;
    }

    @Override
    public Response plan(Request request) {
        //build plan & transport response
        String id = getOrCreateId(request.getId());
        Plan plan = Plan.PlanBuilder.builder(id).data("Simple Plan").compose();
        Response response = Response.ResponseBuilder.builder(id)
                .metadata(new QueryMetadata(id,request.getName(),request.getType(),System.currentTimeMillis()))
                .data(plan)
                .compose();
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteEvent(request, response));
        return response;
    }
}
