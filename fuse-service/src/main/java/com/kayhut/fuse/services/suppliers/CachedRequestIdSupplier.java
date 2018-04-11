package com.kayhut.fuse.services.suppliers;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Created by Roman on 4/9/2018.
 */
public class CachedRequestIdSupplier implements RequestIdSupplier {
    public static final String RequestIdSupplierParameter = "CachedRequestIdSupplier.@requestIdSupplier";

    //region Constructors
    @Inject
    public CachedRequestIdSupplier(
            @Named(RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier) {
        this.requestIdSupplier = requestIdSupplier;
    }
    //endregion

    //region RequestIdSupplier Implementation
    @Override
    public String get() {
        if (this.requestId == null) {
            this.requestId = this.requestIdSupplier.get();
        }

        return this.requestId;
    }
    //endregion

    //region Fields
    private RequestIdSupplier requestIdSupplier;
    private String requestId;
    //endregion
}
