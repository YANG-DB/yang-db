package com.kayhut.fuse.executor.cursor;

import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorContext implements CursorFactory.Context {
    //region Constructor
    public TraversalCursorContext(Ontology ontology, QueryResource queryResource, Traversal<Element, Path> traversal) {
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
    public Traversal<Element, Path> getTraversal() {
        return this.traversal;
    }

    public Ontology getOntology() {
        return ontology;
    }
    //endregion

    private Ontology ontology;
    //region Fields
    private QueryResource queryResource;
    private Traversal<Element, Path> traversal;
    //endregion
}
