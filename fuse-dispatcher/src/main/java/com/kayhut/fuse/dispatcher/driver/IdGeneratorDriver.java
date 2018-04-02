package com.kayhut.fuse.dispatcher.driver;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public interface IdGeneratorDriver<TId> {
    TId getNext(String genName, int numIds);
}
