package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.CursorDispatcherDriver;
import com.kayhut.fuse.model.transport.BaseContent;
import com.kayhut.fuse.model.transport.CursorFetchRequest;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.typesafe.config.Config;

import static com.kayhut.fuse.model.Utils.baseUrl;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleCursorController implements CursorController {

    private Config conf;
    private EventBus eventBus;
    private CursorDispatcherDriver driver;

    @Inject
    public SimpleCursorController(Config conf, EventBus eventBus, CursorDispatcherDriver driver) {
        this.conf = conf;
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.driver = driver;
    }


    @Override
    public ContentResponse fetch(String cursorId, CursorFetchRequest request) {
        int requestSequenceNumber = getNextCursorSequence(cursorId);
        //publish execution isCompleted
        Object fetch = driver.fetch(cursorId, request.getFetchSize());
        String host = baseUrl(conf.getString("application.port"));
        ContentResponse response = ContentResponse.ResponseBuilder.builder(cursorId)
                .data(BaseContent.of(host +  "/"+cursorId+"/result/" + requestSequenceNumber))
                .compose();
        return response;
    }

    @Override
    public ContentResponse plan(String cursorId) {
        //publish execution isCompleted
        Object fetch = driver.plan(cursorId);
        String host = baseUrl(conf.getString("application.port"));
        ContentResponse response = ContentResponse.ResponseBuilder.builder(cursorId)
                .data(BaseContent.of(host +  "/"+cursorId+"/plan/" ))
                .compose();
        return response;
    }

    @Override
    public ContentResponse cancelFetch(String cursorId) {
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
    public ContentResponse delete(String cursorId) {
        //todo delete open cursor
        return ContentResponse.ResponseBuilder.builder(cursorId).compose();
    }
}
