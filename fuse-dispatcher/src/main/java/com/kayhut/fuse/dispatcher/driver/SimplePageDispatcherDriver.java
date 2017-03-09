package com.kayhut.fuse.dispatcher.driver;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.process.PageResourceInfo;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by User on 08/03/2017.
 */
public class SimplePageDispatcherDriver implements PageDispatcherDriver {
    //region Constructors
    @Inject
    public SimplePageDispatcherDriver(EventBus eventBus, ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        this.eventBus = eventBus;
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region PageDispatcherDriver Implementation
    @Override
    public Optional<PageResourceInfo> create(String queryId, String cursorId, int pageSize) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        String pageId = cursorResource.get().getNextPageId();
        submit(this.eventBus, new PageCreationOperationContext(cursorResource.get(), pageId, pageSize));

        return Optional.of(new PageResourceInfo(urlSupplier.resourceUrl(queryId, cursorId, pageId), pageSize, 0));
    }

    @Override
    public Optional<Object> get(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);

        if(!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Optional<Object> pageResource = cursorResource.get().getPageResource(pageId);
        if (!pageResource.isPresent()) {
            return Optional.empty();
        }

        return pageResource;
    }

    @Override
    public Optional<Boolean> delete(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);

        if(!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Optional<Object> pageResource = cursorResource.get().getPageResource(pageId);
        if (!pageResource.isPresent()) {
            return Optional.empty();
        }

        cursorResource.get().deletePageResource(pageId);
        return Optional.of(true);
    }
    //endregion

    //region Fields
    protected EventBus eventBus;
    protected ResourceStore resourceStore;
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
