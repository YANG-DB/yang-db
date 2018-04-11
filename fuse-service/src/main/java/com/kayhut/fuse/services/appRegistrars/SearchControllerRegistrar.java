package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.SearchController;
import org.jooby.Jooby;
import org.jooby.Results;

public class SearchControllerRegistrar extends AppControllerRegistrarBase<SearchController> {
    //region Constructors
    public SearchControllerRegistrar() {
        super(SearchController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** submit a search */
        app.use("/fuse/search")
                .post(req -> {
                    ContentResponse search = this.getController(app).search(req.body(CreateQueryRequest.class));
                    return Results.with(search, search.status());
                });
    }
    //endregion
}
