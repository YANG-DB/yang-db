package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.CursorDispatcherDriver;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
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
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        Optional<CursorResourceInfo> resourceInfo = this.driver.create(queryId, createCursorRequest.getCursorType());
        if (!resourceInfo.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<CursorResourceInfo>builder(UUID.randomUUID().toString())
                .data(resourceInfo.get()).compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        Optional<StoreResourceInfo> resourceInfo = this.driver.getInfo(queryId);
        if (!resourceInfo.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<StoreResourceInfo>builder(UUID.randomUUID().toString())
                .data(resourceInfo.get()).compose();
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        Optional<CursorResourceInfo> resourceInfo = this.driver.getInfo(queryId, cursorId);
        if (!resourceInfo.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<CursorResourceInfo>builder(UUID.randomUUID().toString())
                .data(resourceInfo.get()).compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        Optional<Boolean> isDeleted = this.driver.delete(queryId, cursorId);
        if (!isDeleted.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<Boolean>builder(UUID.randomUUID().toString())
                .data(isDeleted.get()).compose();
    }
}
