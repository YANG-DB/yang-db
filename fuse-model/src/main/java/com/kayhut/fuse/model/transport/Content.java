package com.kayhut.fuse.model.transport;

/**
 * Created by lior on 19/02/2017.
 */
public interface Content<T> {
    String getId();
    T getData();
}
