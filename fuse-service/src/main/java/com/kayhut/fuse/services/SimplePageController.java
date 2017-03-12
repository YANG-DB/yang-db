package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.driver.PageDispatcherDriver;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import org.jooby.Status;

import java.util.UUID;

import static com.kayhut.fuse.model.transport.ContentResponse.Builder.builder;
import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

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
        return Builder.<PageResourceInfo>builder(randomUUID().toString(), CREATED, SERVER_ERROR )
                .data(this.driver.create(queryId, cursorId, createPageRequest.getPageSize()))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryid, String cursorId) {
        return Builder.<StoreResourceInfo>builder(randomUUID().toString(),FOUND, NOT_FOUND)
                .data(this.driver.getInfo(queryid, cursorId))
                .compose();
    }

    @Override
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        return Builder.<PageResourceInfo>builder(randomUUID().toString(),FOUND, NOT_FOUND)
                .data(this.driver.getInfo(queryId, cursorId, pageId))
                .compose();
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        return builder(randomUUID().toString(),FOUND, NOT_FOUND)
                .data(this.driver.getData(queryId, cursorId, pageId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId) {
        return Builder.<Boolean>builder(randomUUID().toString(),ACCEPTED, NOT_FOUND)
                .data(this.driver.delete(queryId, cursorId, pageId))
                .compose();
    }
    //endregion

    //region Fields
    private PageDispatcherDriver driver;
    //endregion
}
