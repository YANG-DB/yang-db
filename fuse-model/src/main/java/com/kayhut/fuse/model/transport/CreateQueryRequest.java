package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.query.Query;

/**
 * Created by lior on 19/02/2017.
 * <p>
 * Mutable structure due to json reflective builder needs...
 */
public class CreateQueryRequest {
    //region Properties
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public Query getQuery() {
        return query;
    }
    //endregion

    //region Fields
    private String id;
    private String name;
    private Query query;
    //endregion
}
