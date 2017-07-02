package com.kayhut.fuse.dispatcher.resource;

import java.util.Optional;

/**
 * Created by User on 06/03/2017.
 */
public interface ResourceStore {
    Iterable<QueryResource> getQueryResources();
    Optional<QueryResource> getQueryResource(String queryId);
    void addQueryResource(QueryResource queryResource);
    void deleteQueryResource(String queryId);
}
