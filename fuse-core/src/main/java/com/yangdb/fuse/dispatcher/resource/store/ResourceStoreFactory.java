package com.yangdb.fuse.dispatcher.resource.store;

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
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.transport.CreateQueryRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ResourceStoreFactory implements ResourceStore {
    public static final String injectionName = "ResourceStoreFactory.persistant";
    private Collection<ResourceStore> stores;

    @Inject
    public ResourceStoreFactory(
            @Named(injectionName) ResourceStore store) {
        this.stores = new ArrayList<>();
        //default in memory store
        Arrays.asList(new ResourceStore[]{new PersistentLocalFileResourceStore(), store})
                .forEach(((ArrayList<ResourceStore>) this.stores)::add);
    }

    @Override
    public Collection<QueryResource> getQueryResources() {
        return stores.stream().flatMap(store -> store.getQueryResources().stream()).collect(Collectors.toList());
    }

    @Override
    public Collection<QueryResource> getQueryResources(Predicate<String> predicate) {
        return stores.stream().flatMap(store -> store.getQueryResources(predicate).stream()).collect(Collectors.toList());
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        if (queryId == null) return Optional.empty();
        return stores.stream().map(store -> store.getQueryResource(queryId))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        if (queryId == null || cursorId == null) return Optional.empty();
        return stores.stream().map(store -> store.getCursorResource(queryId, cursorId))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        if (queryId == null || cursorId == null || pageId == null) return Optional.empty();
        return stores.stream().map(store -> store.getPageResource(queryId, cursorId, pageId))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        return stores.stream().filter(store -> store.test(queryResource.getQueryMetadata().getStorageType()))
                .findFirst().orElse(stores.iterator().next())
                .addQueryResource(queryResource);
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        if (queryId == null) return false;
        return stores.stream().anyMatch(store -> store.deleteQueryResource(queryId));
    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        if (queryId == null) return false;
        return stores.stream().filter(store -> store.getQueryResource(queryId).isPresent())
                .findFirst().orElse(stores.iterator().next())
                .addCursorResource(queryId, cursorResource);
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        if (queryId == null || cursorId == null) return false;
        return stores.stream().anyMatch(store -> store.deleteCursorResource(queryId, cursorId));
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        if (queryId == null || cursorId == null) return false;
        return stores.stream().filter(store -> store.getQueryResource(queryId).isPresent())
                .findFirst().orElse(stores.iterator().next())
                .addPageResource(queryId, cursorId, pageResource);
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        if (queryId == null || cursorId == null || pageId == null) return false;
        return stores.stream().anyMatch(store -> store.deletePageResource(queryId, cursorId, pageId));
    }

    @Override
    public boolean test(CreateQueryRequest.StorageType type) {
        return false;
    }
}
