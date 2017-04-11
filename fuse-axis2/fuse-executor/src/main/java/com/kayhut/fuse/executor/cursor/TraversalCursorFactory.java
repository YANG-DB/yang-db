package com.kayhut.fuse.executor.cursor;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.executor.translation.TraversalCursorQueryResultsTranslator;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.results.QueryResult;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorFactory implements CursorFactory {
    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        return new TraversalCursor(traversalCursorContext);
    }
    //endregion

    /**
     * Created by liorp on 3/20/2017.
     */
    public static class TraversalCursor implements Cursor {

        public TraversalCursor(TraversalCursorContext context) {
            this.context = context;
        }
        //endregion

        //region Cursor Implementation
        @Override
        public QueryResult getNextResults(int numResults) {
            return TraversalCursorQueryResultsTranslator.translatePath(context);
        }
        //endregion

        //region Properties

        public TraversalCursorContext getContext() {
            return context;
        }
        //endregion

        //region Fields
        private TraversalCursorContext context;
        //endregion
    }

    /**
     * Created by Roman on 05/04/2017.
     */
    public static class TraversalCursorContext implements Context {
        //region Constructor
        public TraversalCursorContext(Ontology ontology, QueryResource queryResource, Traversal traversal) {
            this.ontology = ontology;
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

        public Ontology getOntology() {
            return ontology;
        }
        //endregion

        private Ontology ontology;
        //region Fields
        private QueryResource queryResource;
        private Traversal traversal;
        //endregion
    }
}
