package com.kayhut.fuse.unipop;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by User on 07/03/2017.
 */
public interface Constraint {
    static TraversalConstraint by(Traversal traversal) {
        return new TraversalConstraint(traversal);
    }
}
