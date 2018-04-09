package com.kayhut.fuse.dispatcher.driver;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultBase;
import javaslang.collection.Stream;

import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public abstract class PageDriverBase implements PageDriver {
    //region Constructors
    @Inject
    public PageDriverBase(ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region PageDriver Implementation
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

        PageResource<QueryResultBase> pageResource = this.createResource(queryResource.get(), cursorResource.get(), pageId, pageSize);
        this.resourceStore.addPageResource(queryId, cursorId, pageResource);

        return Optional.of(new PageResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId, pageId),
                pageId,
                pageSize,
                pageResource.getActualSize(),
                0,
                true));
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

        return Optional.ofNullable(pageResource.get().getData());
    }
    //endregion

    //region Protected Abstract Methods
    protected abstract PageResource<QueryResultBase> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize);
    //endregion

    //region Fields
    protected ResourceStore resourceStore;
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
