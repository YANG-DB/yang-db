package com.kayhut.fuse.dispatcher.descriptor;

/**
 * Created by moti on 6/19/2017.
 */
public interface QueryDescriptor<Q> {
    String getName(Q query);
    String getPattern(Q query);
}
