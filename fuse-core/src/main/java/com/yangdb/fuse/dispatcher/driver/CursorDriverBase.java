package com.yangdb.fuse.dispatcher.driver;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.collection.Stream;

import java.util.Collections;
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
        //outer query cursor id
        String cursorId = queryResource.get().getNextCursorId();
        //inner cursors for inner queries
        createInnerCursor(queryResource.get(),cursorRequest);
        CursorResource resource = this.createResource(queryResource.get(), cursorId, cursorRequest);
        this.resourceStore.addCursorResource(queryId, resource);

        return Optional.of(new CursorResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId),
                cursorId,
                cursorRequest,
                urlSupplier.pageStoreUrl(queryId, cursorId)));
    }

    private void createInnerCursor(QueryResource query, CreateCursorRequest cursorRequest) {
        Iterable<QueryResource> innerQueryResources = query.getInnerQueryResources();
        innerQueryResources.forEach(innerQuery->{
            create(innerQuery.getQueryMetadata().getId(),cursorRequest);
        });
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
                cursorResource.get().getProfileInfo().infoData(),
                urlSupplier.pageStoreUrl(queryId, cursorId),
                resourceStore.getPageResource(queryId,cursorId,cursorResource.get().getCurrentPageId()).isPresent() ?
                        Collections.singletonList(
                                pageDriver.getInfo(queryId,cursorId,cursorResource.get().getCurrentPageId()).get()) :
                        Collections.EMPTY_LIST
                ));
    }


    @Override
    public Optional<Boolean> delete(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }
        //try delete inner cursors
        queryResource.get().getInnerQueryResources().forEach(inner->delete(inner.getQueryMetadata().getId(),cursorId));
        //delete outer cursor
        queryResource.get().deleteCursorResource(cursorId);
        return Optional.of(true);
    }
    //endregion

    //region Protected Abstract Methods
    protected abstract CursorResource createResource(QueryResource queryResource, String cursorId, CreateCursorRequest cursorRequest);
    //endregion

    //region Fields
    protected PageDriver pageDriver;
    protected ResourceStore resourceStore;
    protected AppUrlSupplier urlSupplier;
    //endregion
}
