package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.PageDispatcherDriver;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimplePageController implements PageController {
    //region Constructors
    @Inject
    public SimplePageController(EventBus eventBus, PageDispatcherDriver driver) {
        this.driver = driver;
        eventBus.register(this);
    }
    //endregion

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        Optional<PageResourceInfo> resourceInfo = this.driver.create(queryId, cursorId, createPageRequest.getPageSize());
        if (!resourceInfo.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<PageResourceInfo>builder(UUID.randomUUID().toString()).data(resourceInfo.get()).compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryid, String cursorId) {
        Optional<StoreResourceInfo> resourceInfo = this.driver.getInfo(queryid, cursorId);
        if (!resourceInfo.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<StoreResourceInfo>builder(UUID.randomUUID().toString()).data(resourceInfo.get()).compose();
    }

    @Override
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        Optional<PageResourceInfo> pageResource = this.driver.getInfo(queryId, cursorId, pageId);
        if (!pageResource.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<PageResourceInfo>builder(UUID.randomUUID().toString()).data(pageResource.get()).compose();
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        Optional<Object> pageData = this.driver.getData(queryId, cursorId, pageId);
        if (!pageData.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.builder(UUID.randomUUID().toString()).data(pageData.get()).compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId) {
        Optional<Boolean> isDeleted = this.driver.delete(queryId, cursorId, pageId);
        if (!isDeleted.isPresent()) {
            return ContentResponse.NOT_FOUND;
        }

        return ContentResponse.Builder.<Boolean>builder(UUID.randomUUID().toString()).data(isDeleted.get()).compose();
    }
    //endregion

    //region Fields
    private PageDispatcherDriver driver;
    //endregion
}
