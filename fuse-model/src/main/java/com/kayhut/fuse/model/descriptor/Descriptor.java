package com.kayhut.fuse.model.descriptor;


/**
 * Created by moti on 6/19/2017.
 */
public interface Descriptor<Q> {
    String name(Q query);
    String describe(Q query);

}
