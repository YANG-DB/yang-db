package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.QueryDispatcherDriver;
import com.kayhut.fuse.model.process.QueryCursorData;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.ContentResponse;

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
    public ContentResponse query(Request request) {
        //build graph & transport response
        String id = getOrCreateId(request.getId());
        QueryMetadata metadata = new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis());
        QueryCursorData cursorData = driver.process(new QueryData(metadata, request.getQuery()));
        // Get the response from context - actually the event bus is doing sync (serial) method (listeners) invocation until the last element in the execution chain is called
        // and sets the response in the context (QueryDispatcherDriver.response)
        return ContentResponse.ResponseBuilder.builder(cursorData.getResultMetadata().getId())
                .queryMetadata(metadata)
                .resultMetadata(cursorData.getResultMetadata())
                .compose();
    }

}
