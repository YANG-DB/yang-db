package com.kayhut.fuse.epb.plan;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanExtensionStrategy<P,Q> {
    Iterable<P> extendPlan(Optional<P> plan, Q query);
}
