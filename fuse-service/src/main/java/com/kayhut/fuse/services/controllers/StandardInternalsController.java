package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.driver.InternalsDriver;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.services.suppliers.RequestIdSupplier;
import com.kayhut.fuse.services.suppliers.SnowflakeRequestIdSupplier;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.kayhut.fuse.services.suppliers.CachedRequestIdSupplier.RequestIdSupplierParameter;
import static org.jooby.Status.ACCEPTED;
import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardInternalsController implements InternalsController {
    //region Constructors
    @Inject
    public StandardInternalsController(
            InternalsDriver driver,
            @Named(RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier,
            Set<CompositeCursorFactory.Binding> cursorBindings) {
        this.driver = driver;
        this.requestIdSupplier = requestIdSupplier;
        this.cursorBindings = cursorBindings;
    }
    //endregion

    //region InternalsController
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
    public ContentResponse<Map<String, Class<? extends CreateCursorRequest>>> getCursorBindings() {
        return Builder.<Map<String, Class<? extends CreateCursorRequest>>>builder(OK, NOT_FOUND)
                .data(Optional.of(
                        Stream.ofAll(this.cursorBindings).toJavaMap(binding -> new Tuple2<>(binding.getType(), binding.getKlass()))))
                .compose();
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

    //region Fields
    private InternalsDriver driver;
    private RequestIdSupplier requestIdSupplier;
    private Set<CompositeCursorFactory.Binding> cursorBindings;
    //endregion
}
