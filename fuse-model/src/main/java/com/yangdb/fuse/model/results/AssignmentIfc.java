package com.yangdb.fuse.model.results;

import com.yangdb.fuse.model.logical.Edge;
import com.yangdb.fuse.model.logical.Vertex;

import java.util.List;

public interface AssignmentIfc<E extends Vertex, R extends Edge> {
    //region Properties
    List<R> getRelationships();

    List<E> getEntities();
}
