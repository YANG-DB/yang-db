package com.kayhut.fuse.neo4j.executor;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.neo4j.GraphProvider;

/**
 * Created by User on 06/03/2017.
 */
public class Neo4jCursor implements Cursor {
    //region Constructors
    public Neo4jCursor(Neo4jCursorContext context, GraphProvider graphProvider) {
        this.context = context;
        this.graphProvider = graphProvider;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResult getNextResults(int numResults) {
        return NeoGraphUtils.query(graphProvider, this);
    }
    //endregion

    //region Properties
    public Neo4jCursorContext getContext() {
        return context;
    }
    //endregion

    //region Fields
    private Neo4jCursorContext context;
    private GraphProvider graphProvider;
    //endregion
}
