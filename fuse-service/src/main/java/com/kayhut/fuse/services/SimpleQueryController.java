package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.QueryDispatcherDriver;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.process.QueryResourceResult;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.transport.*;

import java.util.Optional;
import java.util.UUID;

import static com.kayhut.fuse.model.Utils.baseUrl;
import static com.kayhut.fuse.model.Utils.getOrCreateId;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleQueryController implements QueryController {

    private EventBus eventBus;
    private QueryDispatcherDriver driver;

    @Inject
    public SimpleQueryController( EventBus eventBus, QueryDispatcherDriver driver ) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.driver = driver;
    }

    @Override
    public ContentResponse<QueryResourceResult> query(QueryRequest request) {
        //build graph & transport response
        String id = getOrCreateId(request.getId());
        QueryMetadata metadata = new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis());
        QueryResourceResult result = driver.process(metadata, request.getQuery());
        // Get the response from context - actually the event bus is doing sync (serial) method (listeners) invocation until the last element in the execution chain is called
        // and sets the response in the context (QueryDispatcherDriver.response)
        return ContentResponse.ResponseBuilder.<QueryResourceResult>builder(request.getId())
                .data(new QueryResourceContent(id, result))
                .compose();
    }

    @Override
    public ContentResponse explain(String queryId) {
        Optional<Plan> plan = this.driver.explain(queryId);
        if (!plan.isPresent()) {
            ContentResponse.ResponseBuilder.<Plan>builder(UUID.randomUUID().toString()).data(new PlanContent("-1", null)).compose();
        }

        return ContentResponse.ResponseBuilder.<Plan>builder(UUID.randomUUID().toString()).data(new PlanContent("1", plan.get())).compose();
    }
}
