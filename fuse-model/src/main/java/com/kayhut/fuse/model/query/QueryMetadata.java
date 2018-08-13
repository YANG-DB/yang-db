package com.kayhut.fuse.model.query;

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
    private String id;
    private String name;
    private long creationTime;
    private long ttl;
    //endregion
}
