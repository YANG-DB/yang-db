package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.query.Query;

/**
 * Created by lior on 19/02/2017.
 * <p>
 * Mutable structure due to json reflective builder needs...
 */
public class QueryRequest {
    private String id;
    private String name;
    private String type;
    private Query query;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Query getQuery() {
        return query;
    }
}
