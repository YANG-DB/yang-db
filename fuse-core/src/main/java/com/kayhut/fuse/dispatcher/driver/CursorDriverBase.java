package com.kayhut.fuse.dispatcher.driver;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseError;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.collection.Stream;

import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public abstract class CursorDriverBase implements CursorDriver {
    //region Constructors
    @Inject
    public CursorDriverBase(ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region CursorDriver Implementation
    @Override
    public Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest cursorRequest) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
                return Optional.of(new CursorResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(), "failed fetching next page for query " + queryId)));
        }

        String cursorId = queryResource.get().getNextCursorId();
        this.resourceStore.addCursorResource(queryId, this.createResource(queryResource.get(), cursorId, cursorRequest));

        return Optional.of(new CursorResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId),
                cursorId,
                cursorRequest,
                urlSupplier.pageStoreUrl(queryId, cursorId)));
    }

    @Override
    public Optional<StoreResourceInfo> getInfo(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Iterable<String> resourceUrls = Stream.ofAll(queryResource.get().getCursorResources())
                .sortBy(CursorResource::getTimeCreated)
                .map(CursorResource::getCursorId)
                .map(cursorId -> this.urlSupplier.resourceUrl(queryId, cursorId))
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.cursorStoreUrl(queryId),queryId, resourceUrls));
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
                cursorId,
                cursorResource.get().getCursorRequest(),
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

    //region Protected Abstract Methods
    protected abstract CursorResource createResource(QueryResource queryResource, String cursorId, CreateCursorRequest cursorRequest);
    //endregion

    //region Fields
    private ResourceStore resourceStore;
    private AppUrlSupplier urlSupplier;
    //endregion
}
