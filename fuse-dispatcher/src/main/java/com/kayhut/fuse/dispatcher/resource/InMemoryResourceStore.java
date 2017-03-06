package com.kayhut.fuse.dispatcher.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public class InMemoryResourceStore implements ResourceStore {
    //region Constructors
    public InMemoryResourceStore() {
        this.queryResources = new HashMap<>();
    }
    //endregion

    //region ResourceStore Implementation
    @Override
    public Optional<QueryResource> getQueryResource(String id) {
        return Optional.ofNullable(this.queryResources.get(id));
    }

    @Override
    public void addQueryResource(QueryResource queryResource) {
        this.queryResources.put(queryResource.getQueryMetadata().getId(), queryResource);
    }
    //endregion

    //region Fields
    private Map<String, QueryResource> queryResources;
    //endregion
}
