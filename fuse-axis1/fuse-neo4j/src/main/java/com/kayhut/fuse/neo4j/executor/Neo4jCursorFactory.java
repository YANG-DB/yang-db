package com.kayhut.fuse.neo4j.executor;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.neo4j.GraphProvider;

/**
 * Created by Roman on 02/04/2017.
 */
public class Neo4jCursorFactory implements CursorFactory {
    //region Constructors
    @Inject
    public Neo4jCursorFactory(GraphProvider graphProvider) {
        this.graphProvider = graphProvider;
    }
    //endregion

    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(CursorFactory.Context context) {
        Neo4jCursorContext neo4jCursorContext = (Neo4jCursorContext)context;
        return new Neo4jCursor(neo4jCursorContext, this.graphProvider);
    }
    //endregion

    //region Fields
    private GraphProvider graphProvider;
    //endregion
}
