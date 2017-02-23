package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.CursorDispatcherDriver;
import com.kayhut.fuse.model.transport.BaseContent;
import com.kayhut.fuse.model.transport.CursorFetchRequest;
import com.kayhut.fuse.model.transport.Response;

import static com.kayhut.fuse.model.Utils.baseUrl;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleCursorController implements CursorController {

    private EventBus eventBus;
    private CursorDispatcherDriver driver;

    @Inject
    public SimpleCursorController(EventBus eventBus, CursorDispatcherDriver driver) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.driver = driver;
    }


    @Override
    public Response fetch(String cursorId, CursorFetchRequest request) {
        int requestSequenceNumber = getNextCursorSequence(cursorId);
        //publish execution isCompleted
        Object fetch = driver.fetch(cursorId, request.getFetchSize());
        Response response = Response.ResponseBuilder.builder(cursorId)
                .data(BaseContent.of(baseUrl() +  "/"+cursorId+"/result/" + requestSequenceNumber))
                .compose();
        return response;
    }

    @Override
    public Response plan(String cursorId) {
        //publish execution isCompleted
        Object fetch = driver.plan(cursorId);
        Response response = Response.ResponseBuilder.builder(cursorId)
                .data(BaseContent.of(baseUrl() +  "/"+cursorId+"/plan/" ))
                .compose();
        return response;
    }

    @Override
    public Response cancelFetch(String cursorId) {
        return null;
    }

    /**
     *
     * @param cursorId
     * @return
     */
    private int getNextCursorSequence(String cursorId) {
        //todo get and increment cursor store for current cursor sequence
        return 0;
    }

    @Override
    public Response delete(String cursorId) {
        //todo delete open cursor
        return Response.ResponseBuilder.builder(cursorId).compose();
    }
}
