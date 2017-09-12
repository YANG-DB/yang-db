package com.kayhut.fuse.unipop.controller.promise.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;

import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public interface EdgeConstraintContext {
    Optional<TraversalConstraint> getEdgeConstraint();
}
