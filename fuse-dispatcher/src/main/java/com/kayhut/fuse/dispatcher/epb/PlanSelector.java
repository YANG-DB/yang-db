package com.kayhut.fuse.dispatcher.epb;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanSelector<P, Q> {
    Iterable<P> select(Q query, Iterable<P> plans);
}
