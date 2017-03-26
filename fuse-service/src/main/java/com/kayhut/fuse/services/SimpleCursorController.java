package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.CursorDispatcherDriver;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.typesafe.config.Config;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

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
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        return Builder.<CursorResourceInfo>builder(randomUUID().toString(),CREATED, SERVER_ERROR)
                .data(this.driver.create(queryId, createCursorRequest.getCursorType()))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        return Builder.<StoreResourceInfo>builder(randomUUID().toString(),FOUND, NOT_FOUND)
                .data(this.driver.getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        return Builder.<CursorResourceInfo>builder(randomUUID().toString(),FOUND, NOT_FOUND)
                .data(this.driver.getInfo(queryId, cursorId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        return Builder.<Boolean>builder(randomUUID().toString(),ACCEPTED, NOT_FOUND)
                .data(this.driver.delete(queryId, cursorId)).compose();
    }
}
