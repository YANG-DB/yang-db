package com.kayhut.fuse.unipop.controller.common.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;

import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public interface ConstraintContext {
    Optional<TraversalConstraint> getConstraint();
}
