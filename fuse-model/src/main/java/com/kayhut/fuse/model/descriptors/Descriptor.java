package com.kayhut.fuse.model.descriptors;


/**
 * Created by moti on 6/19/2017.
 */
public interface Descriptor<Q> {
    String describe(Q item);

}