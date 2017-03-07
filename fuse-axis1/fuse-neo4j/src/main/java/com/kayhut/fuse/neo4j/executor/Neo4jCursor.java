package com.kayhut.fuse.neo4j.executor;

import com.kayhut.fuse.model.process.Cursor;

/**
 * Created by User on 06/03/2017.
 */
public class Neo4jCursor implements Cursor {
    //region Constructors
    public Neo4jCursor(String cypherQuery) {
        this.cypherQuery = cypherQuery;
    }
    //endregion

    //region Properties
    public String getCypherQuery() {
        return this.cypherQuery;
    }
    //endregion

    //region Fields
    private String cypherQuery;
    //endregion
}
