package com.kayhut.fuse.model;

/**
 * Created by lior on 19/02/2017.
 */
public interface Content<T> {
    String getId();
    boolean isCompleted();
    long getResults();
    T getData();
}
