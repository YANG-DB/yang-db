package com.kayhut.fuse.model.query;

import com.kayhut.fuse.model.transport.CreateQueryRequest.Type;

/**
 * Created by lior on 21/02/2017.
 */
public final class QueryMetadata {

    public interface QueryMetadataAble {
        QueryMetadata getQueryMetadata();
    }

    private Type type;
    private String id;
    private String name;
    private long time;
    private boolean searchPlan = true;

    public QueryMetadata(String id, String name, long time) {
        this.id = id;
        this.name = name;
        this.time = time;
    }

    public QueryMetadata(Type type,String id, String name, boolean searchPlan ,long time) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.searchPlan = searchPlan;
        this.time = time;
    }

    public boolean isSearchPlan() {
        return searchPlan;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }
}
