package com.kayhut.fuse.dispatcher.driver;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import javaslang.collection.Stream;

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
        submit(this.eventBus, new PageCreationOperationContext(cursorResource.get(), pageId, pageSize)
                .of(new PageResource(pageId, null, pageSize,0)));

        return Optional.of(new PageResourceInfo(urlSupplier.resourceUrl(queryId, cursorId, pageId), pageId, pageSize, 0,0, false));
    }

    @Override
    public Optional<StoreResourceInfo> getInfo(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if(!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Iterable<String> resourceUrls = Stream.ofAll(cursorResource.get().getPageResources())
                .sortBy(pageResource -> pageResource.getTimeCreated())
                .map(pageResource -> pageResource.getPageId())
                .map(pageId -> this.urlSupplier.resourceUrl(queryId, cursorId, pageId))
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.pageStoreUrl(queryId, cursorId),null, resourceUrls));
    }

    @Override
    public Optional<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if(!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Optional<PageResource> pageResource = cursorResource.get().getPageResource(pageId);
        if (!pageResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new PageResourceInfo(this.urlSupplier.resourceUrl(queryId, cursorId, pageId),
                pageId,
                pageResource.get().getRequestedSize(),
                pageResource.get().getActualSize(),
                pageResource.get().getExecutionTime(),
                pageResource.get().isAvailable()));
    }

    @Override
    public Optional<Object> getData(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if(!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Optional<PageResource> pageResource = cursorResource.get().getPageResource(pageId);
        if (!pageResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(pageResource.get().getData());
    }
    //endregion

    //region Fields
    protected EventBus eventBus;
    protected ResourceStore resourceStore;
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
