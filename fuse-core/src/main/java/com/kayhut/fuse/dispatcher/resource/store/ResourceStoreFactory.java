package com.kayhut.fuse.dispatcher.resource.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceStoreFactory implements ResourceStore {
    public static final String injectionName = "ResourceStoreFactory.persistant";
    private Collection<ResourceStore> stores;

    @Inject
    public ResourceStoreFactory(
            @Named(injectionName) ResourceStore store) {
        this.stores = new ArrayList<>();
        //default in memory store
        Arrays.asList(new ResourceStore[]{new InMemoryResourceStore(), store})
                .forEach(((ArrayList<ResourceStore>) this.stores)::add);
    }

    @Override
    public Collection<QueryResource> getQueryResources() {
        return stores.stream().flatMap(store -> store.getQueryResources().stream()).collect(Collectors.toList());
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        return stores.stream().map(store -> store.getQueryResource(queryId))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        return stores.stream().map(store -> store.getCursorResource(queryId, cursorId))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        return stores.stream().map(store -> store.getPageResource(queryId, cursorId, pageId))
                .filter(Optional::isPresent)
                .findFirst().orElse(Optional.empty());
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        return stores.stream().filter(store -> store.test(queryResource.getQueryMetadata().getType()))
                .findFirst().orElse(stores.iterator().next())
                .addQueryResource(queryResource);
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        return stores.stream().filter(store -> store.deleteQueryResource(queryId)).findFirst().isPresent();
    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        return stores.stream().filter(store -> store.getQueryResource(queryId).isPresent())
                .findFirst().orElse(stores.iterator().next())
                .addCursorResource(queryId, cursorResource);
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        return stores.stream().filter(store -> store.deleteCursorResource(queryId, cursorId)).findFirst().isPresent();
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        return stores.stream().filter(store -> store.getQueryResource(queryId).isPresent())
                .findFirst().orElse(stores.iterator().next())
                .addPageResource(queryId, cursorId, pageResource);
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        return stores.stream().filter(store -> store.deletePageResource(queryId, cursorId, pageId)).findFirst().isPresent();
    }

    @Override
    public boolean test(CreateQueryRequest.Type type) {
        return false;
    }
}
