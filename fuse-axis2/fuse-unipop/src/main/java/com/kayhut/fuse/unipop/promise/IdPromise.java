package com.kayhut.fuse.unipop.promise;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by User on 07/03/2017.
 */
public class IdPromise implements Promise {
    //region Constructor
    public IdPromise(Object id) {
        this.id = id;
    }
    //endregion

    //region Promise Implementation
    @Override
    public Object getId() {
        return id;
    }
    //endregion

    //region Modulation
    public TraversalPromise by(Traversal traversal) {
        return new TraversalPromise(this.id, traversal);
    }
    //endregion

    //region fields
    private Object id;
    //endregion
}
