package com.kayhut.fuse.unipop.controller;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.unipop.query.search.SearchVertexQuery;

import java.util.Iterator;

/**
 * Created by User on 16/03/2017.
 */
public class SearchPromiseVertexController implements SearchVertexQuery.SearchVertexController {
    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        return null;
    }
    //endregion
}
