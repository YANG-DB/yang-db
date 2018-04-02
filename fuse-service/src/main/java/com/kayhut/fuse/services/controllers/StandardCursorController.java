package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.*;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardCursorController implements CursorController {
    //region Constructors
    @Inject
    public StandardCursorController(CursorDriver driver) {
        this.driver = driver;
    }
    //endregion

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        return Builder.<CursorResourceInfo>builder(randomUUID().toString(),CREATED, SERVER_ERROR)
                .data(this.driver.create(queryId, createCursorRequest))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        return Builder.<StoreResourceInfo>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(this.driver.getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        return Builder.<CursorResourceInfo>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(this.driver.getInfo(queryId, cursorId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        return Builder.<Boolean>builder(randomUUID().toString(),ACCEPTED, NOT_FOUND)
                .data(this.driver.delete(queryId, cursorId)).compose();
    }
    //endregion

    //region Fields
    private CursorDriver driver;
    //endregion
}
