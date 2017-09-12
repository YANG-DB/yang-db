package com.kayhut.fuse.unipop.controller.discrete.context;

import com.kayhut.fuse.unipop.promise.TraversalConstraint;

import java.util.Optional;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementControllerContext {
    //region Constructors
    public DiscreteElementControllerContext(
            Optional<TraversalConstraint> constraint) {
        this.constraint = constraint;
    }
    //endregion

    //region Properties
    public Optional<TraversalConstraint> getConstraint() {
        return constraint;
    }
    //endregion

    //region Fields
    private Optional<TraversalConstraint> constraint;
    //endregion
}
