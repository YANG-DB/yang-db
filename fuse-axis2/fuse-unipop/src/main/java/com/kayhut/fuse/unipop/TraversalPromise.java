package com.kayhut.fuse.unipop;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by User on 07/03/2017.
 */
public class TraversalPromise implements Promise{
    //region Constructor
    public TraversalPromise(Object id, Traversal traversal) {
        this.id = id;
        this.traversal = traversal;
    }
    //endregion

    //region Promise Implementation
    public Object getId() {
        return id;
    }
    //endregion

    //region properties
    public Traversal getTraversal() {
        return this.traversal;
    }
    //endregion

    @Override
    public String toString() {
        return getId().toString() + ": " + traversal.toString();
    }


    //region fields
    private Object id;
    private Traversal traversal;
    //endregion
}
