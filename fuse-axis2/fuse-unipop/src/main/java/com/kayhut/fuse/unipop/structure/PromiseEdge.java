package com.kayhut.fuse.unipop.structure;

import com.kayhut.fuse.unipop.controller.GlobalConstants;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.Map;

/**
 * Created by r on 11/16/2015.
 */
public class PromiseEdge extends UniEdge{

    //region Constructor
    public PromiseEdge(Vertex outV, Vertex inV, Map<String, Object> properties, UniGraph graph) {
        super(properties, outV, inV, graph);
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return "promise";
    }

    @Override
    public String toString() {
        return String.format(PRINT_FORMAT, outVertex.id(), id, property(GlobalConstants.HasKeys.COUNT), inVertex.id());
    }
    //endregion

    //region Static
    private static String PRINT_FORMAT = "%s --(%s: %s)--> %s";
    //endregion
}
