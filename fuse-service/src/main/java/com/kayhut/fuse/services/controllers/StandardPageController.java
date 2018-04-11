package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.driver.PageDriver;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.CreatePageRequest;

import java.util.Optional;

import static com.kayhut.fuse.model.transport.ContentResponse.Builder.builder;
import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardPageController implements PageController {
    //region Constructors
    @Inject
    public StandardPageController(PageDriver driver) {
        this.driver = driver;
    }
    //endregion

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        return Builder.<PageResourceInfo>builder(CREATED, SERVER_ERROR )
                .data(this.driver.create(queryId, cursorId, createPageRequest.getPageSize()))
                .compose();
    }

    @Override
    public ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        ContentResponse<PageResourceInfo> pageResourceInfoResponse = this.create(queryId, cursorId, createPageRequest);
        if (pageResourceInfoResponse.status() == SERVER_ERROR) {
            return Builder.<PageResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(pageResourceInfoResponse.getData()))
                    .successPredicate(response -> false)
                    .compose();
        }

        ContentResponse<Object> pageDataResponse = this.getData(queryId, cursorId, pageResourceInfoResponse.getData().getResourceId());
        if (pageDataResponse.status() == SERVER_ERROR) {
            return Builder.<PageResourceInfo>builder(CREATED, SERVER_ERROR)
                    .data(Optional.of(pageResourceInfoResponse.getData()))
                    .successPredicate(response -> false)
                    .compose();
        }

        pageResourceInfoResponse.getData().setData(pageDataResponse.getData());

        return Builder.<PageResourceInfo>builder(CREATED, SERVER_ERROR)
                .data(Optional.of(pageResourceInfoResponse.getData()))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryid, String cursorId) {
        return Builder.<StoreResourceInfo>builder(OK, NOT_FOUND)
                .data(this.driver.getInfo(queryid, cursorId))
                .compose();
    }

    @Override
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        return Builder.<PageResourceInfo>builder(OK, NOT_FOUND)
                .data(this.driver.getInfo(queryId, cursorId, pageId))
                .compose();
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        return builder(OK, NOT_FOUND)
                .data(this.driver.getData(queryId, cursorId, pageId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId) {
        return ContentResponse.Builder.<Boolean>builder(ACCEPTED, NOT_FOUND)
                .data(this.driver.delete(queryId, cursorId, pageId))
                .compose();
    }

    //endregion

    //region Fields
    private PageDriver driver;
    //endregion
}
