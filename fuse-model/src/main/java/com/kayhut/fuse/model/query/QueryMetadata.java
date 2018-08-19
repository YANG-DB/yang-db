package com.kayhut.fuse.model.query;

import com.kayhut.fuse.model.transport.CreateQueryRequest.Type;

/**
 * Created by lior on 21/02/2017.
 */
public final class QueryMetadata {

    public interface QueryMetadataAble {
        QueryMetadata getQueryMetadata();
    }

    //region Properties
    public QueryMetadata(Type type,String id, String name, boolean searchPlan ,long creationTime,long ttl) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.searchPlan = searchPlan;
        this.creationTime = creationTime;
        this.ttl = ttl;
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

    public long getCreationTime() {
        return creationTime;
    }

    public long getTtl() {
        return ttl;
    }
    //endregion

    //region Fields
    private long creationTime;
    private long ttl;
    private String id;
    private String name;
    private boolean searchPlan = true;
    private Type type = Type._volatile;
    //endregion
}
