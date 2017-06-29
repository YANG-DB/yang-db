package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.query.Query;

/**
 * Created by lior on 19/02/2017.
 * <p>
 * Mutable structure due to json reflective builder needs...
 */
public class CreateQueryRequest {
    //region Constructors
    public CreateQueryRequest() {

    }

    public CreateQueryRequest(String id, String name, Query query) {
        this.id = id;
        this.name = name;
        this.query = query;
    }

    public CreateQueryRequest(String id, String name, Query query, boolean verbose) {
        this(id, name, query);
        this.verbose = verbose;
    }
    //endregion

    //region Properties
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

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
    private boolean verbose;
    private String name;
    private Query query;
    //endregion
}
