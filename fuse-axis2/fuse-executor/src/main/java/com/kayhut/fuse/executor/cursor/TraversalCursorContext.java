package com.kayhut.fuse.executor.cursor;

import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorContext implements CursorFactory.Context {
    //region Constructor
    public TraversalCursorContext(
            Ontology ontology,
            QueryResource queryResource,
            CreateCursorRequest.CursorType cursorType,
            GraphTraversal<?, Path> traversal) {
        this.ontology = ontology;
        this.queryResource = queryResource;
        this.cursorType = cursorType;
        this.traversal = traversal;
    }
    //endregion

    //region CursorFactory.Context Implementation
    @Override
    public QueryResource getQueryResource() {
        return this.queryResource;
    }

    @Override
    public CreateCursorRequest.CursorType getCursorType() {
        return this.cursorType;
    }
    //endregion

    //region Properties
    public GraphTraversal<?, Path> getTraversal() {
        return this.traversal;
    }

    public Ontology getOntology() {
        return ontology;
    }
    //endregion

    //region Fields
    private Ontology ontology;
    private QueryResource queryResource;
    private CreateCursorRequest.CursorType cursorType;
    private GraphTraversal<?, Path> traversal;
    //endregion
}
