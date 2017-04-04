package com.kayhut.fuse.neo4j.executor;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;

/**
 * Created by Roman on 02/04/2017.
 */
public class Neo4jCursorContext implements CursorFactory.Context {
    //region Constructors
    public Neo4jCursorContext(QueryResource queryResource, String cypher) {
        this.queryResource = queryResource;
        this.cypher = cypher;
    }
    //endregion

    //region Properties
    @Override
    public QueryResource getQueryResource() {
        return this.queryResource;
    }

    public String getCypher() {
        return cypher;
    }
    //endregion

    //region Fields
    private QueryResource queryResource;
    private String cypher;
    private boolean isValid;
    //endregion
}
