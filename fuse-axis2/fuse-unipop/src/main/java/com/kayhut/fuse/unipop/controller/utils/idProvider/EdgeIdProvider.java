package com.kayhut.fuse.unipop.controller.utils.idProvider;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

/**
 * Created by Roman on 15/05/2017.
 */
public interface EdgeIdProvider<T>  {
    T get(String edgeLabel, Vertex outV, Vertex inV, Map<String, Object> properties);
}
