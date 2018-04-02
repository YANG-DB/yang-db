package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.driver.InternalsDriver;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.ACCEPTED;
import static org.jooby.Status.NOT_FOUND;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardInternalsController implements InternalsController{
    //region Constructors
    @Inject
    public StandardInternalsController(InternalsDriver driver) {
        this.driver = driver;
    }
    //endregion

    //region Fields
    private InternalsDriver driver;

    @Override
    public ContentResponse<String> getStatisticsProviderName() {
        return Builder.<String>builder(randomUUID().toString(),ACCEPTED, NOT_FOUND)
                .data(this.driver.getStatisticsProviderName()).compose();
    }

    @Override
    public ContentResponse<String> getStatisticsProviderSetup() {
        return Builder.<String>builder(randomUUID().toString(),ACCEPTED, NOT_FOUND)
                .data(this.driver.getStatisticsProviderSetup()).compose();
    }

    @Override
    public ContentResponse<String> refreshStatisticsProviderSetup() {
        return Builder.<String>builder(randomUUID().toString(),ACCEPTED, NOT_FOUND)
                .data(this.driver.refreshStatisticsProviderSetup()).compose();
    }
    //endregion
}
