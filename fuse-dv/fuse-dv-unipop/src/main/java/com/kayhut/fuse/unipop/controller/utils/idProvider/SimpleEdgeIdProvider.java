package com.kayhut.fuse.unipop.controller.utils.idProvider;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

/**
 * Created by roman.margolis on 16/11/2017.
 */
public class SimpleEdgeIdProvider implements EdgeIdProvider<String> {
    //region EdgeIdProvider Implementation
    @Override
    public String get(String edgeLabel, Vertex outV, Vertex inV, Map<String, Object> properties) {
        return String.format("%s.%s.%s", edgeLabel, outV.id(), inV.id());
    }
    //endregion
}
