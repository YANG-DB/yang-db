package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.CREATED;
import static org.jooby.Status.SERVER_ERROR;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class StandardIdGeneratorController<TId> implements IdGeneratorController<TId> {
    //region Constructors
    @Inject
    public StandardIdGeneratorController(IdGeneratorDriver<TId> driver) {
        this.driver = driver;
    }
    //endregion

    //region IdGenerator Implementation
    @Override
    public ContentResponse<TId> getNext(String genName, int numIds) {
        return ContentResponse.Builder.<TId>builder(randomUUID().toString(), CREATED, SERVER_ERROR )
                .data(Optional.of(this.driver.getNext(genName, numIds)))
                .compose();
    }
    //endregion

    //region Fields
    private IdGeneratorDriver<TId> driver;
    //endregion
}
