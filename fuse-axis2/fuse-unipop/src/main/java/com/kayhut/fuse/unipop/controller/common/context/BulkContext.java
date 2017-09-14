package com.kayhut.fuse.unipop.controller.common.context;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface BulkContext {
    Iterable<Vertex> getBulkVertices();
    Vertex getVertex(Object id);
}
