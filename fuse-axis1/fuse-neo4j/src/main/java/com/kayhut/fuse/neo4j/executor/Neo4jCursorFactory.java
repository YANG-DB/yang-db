package com.kayhut.fuse.neo4j.executor;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.results.QueryResult;
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

    /**
     * Created by Roman on 02/04/2017.
     */
    public static class Neo4jCursorContext implements Context {
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

    /**
     * Created by User on 06/03/2017.
     */
    public static class Neo4jCursor implements Cursor {
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
}
