package com.kayhut.fuse.model.process;

/**
 * Created by lior on 21/02/2017.
 */
public final class QueryMetadata {
    private String id;
    private String name;
    private String type;
    private long time;

    public QueryMetadata(String id, String name,String type, long time) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.time = time;
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

    public String getType() {
        return type;
    }
}
