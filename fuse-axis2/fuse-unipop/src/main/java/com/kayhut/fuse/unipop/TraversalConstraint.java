package com.kayhut.fuse.unipop;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

/**
 * Created by User on 07/03/2017.
 */
public class TraversalConstraint extends TraversalPromise implements Constraint {
    //region Static
    public static TraversalConstraint EMPTY = new TraversalConstraint(__.start());
    //endregion

    //region Constructor
    public TraversalConstraint(Traversal traversal) {
        super(null, traversal);
    }
    //endregion
}
