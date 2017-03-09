package com.kayhut.fuse.dispatcher.driver;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.process.CursorResourceInfo;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class SimpleCursorDispatcherDriver implements CursorDispatcherDriver {
    //region Constructors
    @Inject
    public SimpleCursorDispatcherDriver(EventBus eventBus, ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region CursorDispatcherDriver Implementation
    @Override
    public Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest.CursorType cursorType) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        String cursorId = queryResource.get().getNextCursorId();
        submit(eventBus, new CursorCreationOperationContext(queryResource.get(), cursorId, cursorType));

        return Optional.of(new CursorResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId),
                cursorType,
                urlSupplier.pageStoreUrl(queryId, cursorId)));
    }

    @Override
    public Optional<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new CursorResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId),
                cursorResource.get().getCursorType(),
                urlSupplier.pageStoreUrl(queryId, cursorId)));
    }

    @Override
    public Optional<Boolean> delete(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        queryResource.get().deleteCursorResource(cursorId);
        return Optional.of(true);
    }
    //endregion

    //region Fields
    protected EventBus eventBus;
    protected ResourceStore resourceStore;
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
