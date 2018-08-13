package com.kayhut.fuse.model.query;

import com.kayhut.fuse.model.transport.CreateQueryRequest.Type;

/**
 * Created by lior on 21/02/2017.
 */
public final class QueryMetadata {

    public interface QueryMetadataAble {
        QueryMetadata getQueryMetadata();
    }

    //region Constructors
    public QueryMetadata(String id, String name, long creationTime, long ttl) {
        this.id = id;
        this.name = name;
        this.creationTime = creationTime;
        this.ttl = ttl;
    }
    //endregion

    //region Properties
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
    private Type type;
    private String id;
    private String name;
    private long time;
    private boolean searchPlan = true;
    //endregion
}
