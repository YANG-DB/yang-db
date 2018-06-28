package com.kayhut.fuse.unipop.schemaProviders;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by roman.margolis on 10/10/2017.
 */
public interface GraphElementConstraint {
    Traversal getTraversalConstraint();

    class Impl implements GraphElementConstraint {
        //region Constructors
        public Impl(Traversal traversalConstraint) {
            this.traversalConstraint = traversalConstraint;
        }
        //endregion

        //region GraphElementConstraint Implementation
        @Override
        public Traversal getTraversalConstraint() {
            return this.traversalConstraint;
        }
        //endregion

        //region Fields
        private Traversal traversalConstraint;
        //endregion
    }
}
