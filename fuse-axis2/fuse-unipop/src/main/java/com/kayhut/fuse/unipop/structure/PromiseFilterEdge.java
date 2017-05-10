package com.kayhut.fuse.unipop.structure;

import com.kayhut.fuse.unipop.controller.utils.PromiseEdgeConstants;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Roman on 5/9/2017.
 */
public class PromiseFilterEdge extends UniEdge {

    //region Constructor
    public PromiseFilterEdge(Vertex v,  UniGraph graph) {
        super(Collections.emptyMap(), v, v, graph);
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return "promiseFilter";
    }

    @Override
    public String toString() {
        return String.format(PRINT_FORMAT, outVertex.id(), id, inVertex.id());
    }
    //endregion

    //region Static
    private static String PRINT_FORMAT = "%s --(%s)--> %s";
    //endregion
}
