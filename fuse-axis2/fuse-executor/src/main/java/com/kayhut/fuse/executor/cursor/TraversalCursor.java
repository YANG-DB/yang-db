package com.kayhut.fuse.executor.cursor;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.model.results.QueryResult;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by liorp on 3/20/2017.
 */
public class TraversalCursor implements Cursor {
    //region Constructors
    public TraversalCursor(Traversal traversal) {
        this.traversal = traversal;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResult getNextResults(int numResults) {
        return null;
    }
    //endregion

    //region Properties
    public Traversal getTraversal() {
        return traversal;
    }
    //endregion

    //region Fields
    private Traversal traversal;
    //endregion
}
