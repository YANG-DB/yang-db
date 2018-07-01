package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.driver.InternalsDriver;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.services.suppliers.SnowflakeRequestIdSupplier;

import java.util.Optional;

import static com.kayhut.fuse.services.suppliers.CachedRequestIdSupplier.RequestIdSupplierParameter;
import static org.jooby.Status.ACCEPTED;
import static org.jooby.Status.NOT_FOUND;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardInternalsController implements InternalsController {
    //region Constructors
    @Inject
    public StandardInternalsController(InternalsDriver driver, @Named(RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier) {
        this.driver = driver;
        this.requestIdSupplier = requestIdSupplier;
    }
    //endregion

    //region Fields
    private InternalsDriver driver;
    private RequestIdSupplier requestIdSupplier;

    @Override
    public ContentResponse<String> getVersion() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(this.getClass().getPackage().getImplementationVersion()))
                .compose();
    }

    @Override
    public ContentResponse<Long> getSnowflakeId() {
        return Builder.<Long>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(((SnowflakeRequestIdSupplier) requestIdSupplier)
                        .getWorkerId())).compose();
    }

    @Override
    public ContentResponse<String> getStatisticsProviderName() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(this.driver.getStatisticsProviderName()).compose();
    }

    @Override
    public ContentResponse<String> getStatisticsProviderSetup() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(this.driver.getStatisticsProviderSetup()).compose();
    }

    @Override
    public ContentResponse<String> refreshStatisticsProviderSetup() {
        return Builder.<String>builder(ACCEPTED, NOT_FOUND)
                .data(this.driver.refreshStatisticsProviderSetup()).compose();
    }
    //endregion
}
