package com.kayhut.fuse.model;

/**
 * Created by lior on 19/02/2017.
 */
public abstract class Metadata {
    private final String id;
    private final String name;
    private final String status;
    private final long timestamp;

    public Metadata(String id, String name, String status, long timestamp) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.timestamp = timestamp;
    }
}
