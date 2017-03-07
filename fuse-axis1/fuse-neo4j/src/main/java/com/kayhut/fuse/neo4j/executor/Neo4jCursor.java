package com.kayhut.fuse.neo4j.executor;

import com.kayhut.fuse.model.process.Cursor;
import com.kayhut.fuse.model.query.Query;

/**
 * Created by User on 06/03/2017.
 */
public class Neo4jCursor implements Cursor {
    //region Constructors
    public Neo4jCursor(Query query) {
        this.query = query;
    }
    //endregion

    //region Properties
    public Query getQuery() {
        return this.query;
    }
    //endregion

    //region Fields
    private Query query;
    //endregion
}
