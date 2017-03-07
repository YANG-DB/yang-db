package com.kayhut.fuse.unipop;

/**
 * Created by User on 07/03/2017.
 */
public interface Promise {
    Object getId();

    static IdPromise as(String id) {
        return new IdPromise(id);
    }
}
