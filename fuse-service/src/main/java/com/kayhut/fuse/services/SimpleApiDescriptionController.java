package com.kayhut.fuse.services;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

/**
 * Created by Roman on 11/06/2017.
 */
public class SimpleApiDescriptionController implements ApiDescriptionController {
    //region Constructors
    @Inject
    public SimpleApiDescriptionController(AppUrlSupplier urlSupplier) {
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        return ContentResponse.Builder.<FuseResourceInfo>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(Optional.of(new FuseResourceInfo(
                        "/fuse",
                        "/fuse/health",
                        this.urlSupplier.queryStoreUrl(),
                        "/fuse/search",
                        this.urlSupplier.catalogStoreUrl())))
                .compose();
    }
    //endregion

    //region Fields
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
