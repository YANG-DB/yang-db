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
import com.yangdb.fuse.client.export.GraphWriter;
import com.yangdb.fuse.client.export.GraphWriterStrategy;
import com.yangdb.fuse.client.export.graphml.GraphMLWriter;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import javaslang.collection.Stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;

/**
 * Created by Roman on 12/15/2017.
 */
public abstract class PageDriverBase implements PageDriver {

    //todo replace with proper writers injected via IOC
    private GraphWriterStrategy writerStrategy;


    //region Constructors
    @Inject
    public PageDriverBase(ResourceStore resourceStore, AppUrlSupplier urlSupplier, GraphWriterStrategy writerStrategy) {
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
        this.writerStrategy = writerStrategy;

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
        createInnerPage(queryResource.get(), cursorId, pageSize);
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
        queryResource.getInnerQueryResources().forEach(inner -> {
            create(inner.getQueryMetadata().getId(), cursorId, pageSize);
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
                pageResource.get().isAvailable(),
                pageResource.get().getData()));
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
    public Optional<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        if (cursorResource.get().getCursorRequest() instanceof CreateGraphCursorRequest) {
            try {
                AssignmentsQueryResult result = (AssignmentsQueryResult) getData(queryId, cursorId, pageId).get();
                Assignment graph = (Assignment) result.getAssignments().get(0);
                if(writerStrategy.writer(format).isPresent()) {
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    writerStrategy.writer(format).get().writeGraph(stream,graph);
                    return Optional.of(new String(stream.toByteArray()));
                }
                return Optional.of(result);
            } catch (IOException e) {
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),e)));
            }
        }
        return Optional.empty();

    }

    @Override
    public Optional<Boolean> delete(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        //delete inner query pages
        queryResource.get().getInnerQueryResources().forEach(inner -> delete(inner.getQueryMetadata().getId(), cursorId, pageId));
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
