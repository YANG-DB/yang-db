package com.kayhut.fuse.neo4j.executor;

import com.kayhut.fuse.dispatcher.Cursor;
import com.kayhut.fuse.model.query.Query;

/**
 * Created by User on 06/03/2017.
 */
public class Neo4jCursor implements Cursor {
    //region Constructors
    public Neo4jCursor(Query query,String cypher, boolean isValid) {
        this.query = query;
        this.cypher = cypher;
    }
    //endregion

    //region Properties
    public Query getQuery() {
        return this.query;
    }
    //endregion

    public String getCypher() {
        return cypher;
    }

    public boolean isValid() {
        return isValid;
    }

    //region Fields
    private Query query;
    private String cypher;
    private boolean isValid;
    //endregion
}
