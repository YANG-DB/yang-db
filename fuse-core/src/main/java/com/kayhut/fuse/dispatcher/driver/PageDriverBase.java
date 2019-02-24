package com.kayhut.fuse.dispatcher.driver;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.FuseError;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
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

        //outer page id resource
        String pageId = cursorResource.get().getNextPageId();
        //create inner page resources
        createInnerPage(queryResource.get(),cursorId,pageSize);
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

    private void createInnerPage(QueryResource queryResource, String cursorId, int pageSize) {
        queryResource.getInnerQueryResources().forEach(inner->{
            create(inner.getQueryMetadata().getId(),cursorId,pageSize);
        });
    }

    @Override
    public Optional<StoreResourceInfo> getInfo(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
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

        return Optional.of(new StoreResourceInfo(this.urlSupplier.pageStoreUrl(queryId, cursorId), null, resourceUrls));
    }

    @Override
    public Optional<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
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
        if (!queryResource.isPresent()) {
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

    @Override
    public Optional<Boolean> delete(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        //delete inner query pages
        queryResource.get().getInnerQueryResources().forEach(inner->delete(inner.getQueryMetadata().getId(),cursorId,pageId));
        //delete outer resources
        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        cursorResource.get().deletePageResource(pageId);
        return Optional.of(true);
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
