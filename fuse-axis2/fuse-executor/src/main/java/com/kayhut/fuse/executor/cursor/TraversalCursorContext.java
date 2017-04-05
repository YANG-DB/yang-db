package com.kayhut.fuse.executor.cursor;

import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorContext implements CursorFactory.Context {
    //region Constructor
    public TraversalCursorContext(QueryResource queryResource, Traversal traversal) {
        this.queryResource = queryResource;
        this.traversal = traversal;
    }
    //endregion

    //region CursorFactory.Context Implementation
    @Override
    public QueryResource getQueryResource() {
        return this.queryResource;
    }
    //endregion

    //region Properties
    public Traversal getTraversal() {
        return this.traversal;
    }
    //endregion

    //region Fields
    private QueryResource queryResource;
    private Traversal traversal;
    //endregion
}
