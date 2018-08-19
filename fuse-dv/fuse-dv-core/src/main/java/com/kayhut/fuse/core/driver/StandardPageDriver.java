package com.kayhut.fuse.core.driver;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.driver.PageDriverBase;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.results.QueryResultBase;

/**
 * Created by User on 08/03/2017.
 */
public class StandardPageDriver extends PageDriverBase {
    //region Constructors
    @Inject
    public StandardPageDriver(ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        super(resourceStore, urlSupplier);
    }
    //endregion

    //region PageDriverBase Implementation
    @Override
    protected PageResource<QueryResultBase> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize) {
        QueryResultBase results = cursorResource.getCursor().getNextResults(pageSize);
        return new PageResource<>(pageId, results, pageSize, 0)
                        .withActualSize(results.getSize())
                        .available();
    }
    //endregion
}
