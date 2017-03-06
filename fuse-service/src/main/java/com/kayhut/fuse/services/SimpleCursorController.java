package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.CursorDispatcherDriver;
import com.kayhut.fuse.model.process.CursorResourceResult;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CursorResourceContent;
import com.typesafe.config.Config;

import java.util.Optional;
import java.util.UUID;

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
    public ContentResponse fetch(String queryId, int cursorId, long fetchSize) {
        Optional<CursorResourceResult> fetch = driver.fetch(queryId, cursorId, fetchSize);
        if(!fetch.isPresent()) {
            return ContentResponse.EMPTY;
        }

        return ContentResponse.ResponseBuilder.<CursorResourceResult>builder(UUID.randomUUID().toString())
                .data(new CursorResourceContent("1", fetch.get())).compose();
    }
}
