package com.kayhut.fuse.epb.plan;

import javaslang.Tuple2;

/**
 * Created by moti on 2/22/2017.
 */
public interface PlanWrapper<P,C> {
    P getPlan();
    C getPlanCost();
    Tuple2<P,C> asTuple2();
    boolean isPlanComplete();
}
