package com.kayhut.fuse.unipop.structure;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.Map;

/**
 * Created by r on 11/16/2015.
 */
public class PromiseEdge extends UniEdge{
    private final Object id;

    //region Constructor
    public PromiseEdge(Object id, Vertex outV, Vertex inV, Map<String, Object> properties, UniGraph graph) {
        super(properties,outV,inV,graph);
        this.id = id;
    }
    //endregion


    public Object getId() {
        return id;
    }

    @Override
    public String toString() {
        return "e*[" + id() + "]";
    }
    //endregion
}
