package com.yangdb.fuse.dispatcher.resource.store;

/*-
 *
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import javaslang.collection.Stream;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 06/03/2017.
 */
public class InMemoryResourceStore implements ResourceStore {
    //region Constructors
    public InMemoryResourceStore() {
        this.queryResources = new HashMap<>();
        this.sync = new Object();
        this.lastQueryRemovalTime = System.currentTimeMillis();
    }
    //endregion

    //region ResourceStore Implementation
    @Override
    public Collection<QueryResource> getQueryResources() {
        return this.queryResources.values();
    }

    @Override
    public Collection<QueryResource> getQueryResources(Predicate<String> predicate) {
        return this.queryResources.entrySet().stream()
                .filter(e->predicate.test(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<QueryResource> getQueryResource(String id) {
        return Optional.ofNullable(this.queryResources.get(id));
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        return queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        Optional<CursorResource> cursorResource = this.getCursorResource(queryId, cursorId);
        return cursorResource.flatMap(cursorResource1 -> cursorResource1.getPageResource(pageId));
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        this.queryResources.put(queryResource.getQueryMetadata().getId(), queryResource);
        return true;
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        return this.queryResources.remove(queryId)!=null;
    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (queryResource.isPresent()) {
            queryResource.get().addCursorResource(cursorResource.getCursorId(), cursorResource);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (queryResource.isPresent()) {
            queryResource.get().deleteCursorResource(cursorId);
            return true;
        }
        return false;
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return false;
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (cursorResource.isPresent()) {
            cursorResource.get().addPageResource(pageResource.getPageId(), pageResource);
        }
        return true;
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return false;
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (cursorResource.isPresent()) {
            cursorResource.get().deletePageResource(pageId);
        }
        return true;
    }
    //endregion

    //region Private Methods
    private void removeOldQueries() {
        final long currentTime = System.currentTimeMillis();

        if (currentTime - this.lastQueryRemovalTime >= 300000) {
            synchronized (this.sync) {
                List<String> queryKeysToDelete =
                        Stream.ofAll(this.queryResources.entrySet())
                        .filter(entry -> (currentTime - entry.getValue().getQueryMetadata().getCreationTime()) >
                                entry.getValue().getQueryMetadata().getTtl())
                        .map(Map.Entry::getKey)
                        .toJavaList();

                Stream.ofAll(queryKeysToDelete).forEach(this::deleteQueryResource);

                this.lastQueryRemovalTime = currentTime;
            }
        }
    }

    @Override
    public boolean test(CreateQueryRequest.StorageType type) {
        return type.equals(CreateQueryRequest.StorageType._volatile);
    }

//endregion

    //region Fields
    private Map<String, QueryResource> queryResources;

    private long lastQueryRemovalTime;
    private final Object sync;
    //endregion
}
