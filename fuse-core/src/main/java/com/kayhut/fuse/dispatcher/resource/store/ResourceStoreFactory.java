package com.kayhut.fuse.dispatcher.resource.store;

import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceStoreFactory implements ResourceStore{
    private Collection<ResourceStore> stores;

    public ResourceStoreFactory(ResourceStore ... stores) {
        this.stores = new ArrayList<>();
        Arrays.asList(stores).forEach(((ArrayList<ResourceStore>) this.stores)::add);
    }

    @Override
    public Collection<QueryResource> getQueryResources() {
        return stores.stream().flatMap(store->store.getQueryResources().stream()).collect(Collectors.toList());
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        return stores.stream().map(store -> store.getQueryResource(queryId).get()).findFirst();
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        return stores.stream().map(store -> store.getCursorResource(queryId,cursorId).get()).findFirst();
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        return stores.stream().map(store -> store.getPageResource(queryId,cursorId,pageId).get()).findFirst();
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        return false;
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        return false;

    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        return false;

    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        return false;

    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        return false;

    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        return false;

    }
}
