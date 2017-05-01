package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.unipop.controller.utils.PromiseEdgeConstants;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.PromiseEdge;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.unipop.structure.UniGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class SearchHitPromiseEdgeConverter implements ElementConverter<SearchHit, Edge> {

    //region Constructor
    public SearchHitPromiseEdgeConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    @Override
    public Edge convert(SearchHit hit) {
        String id = hit.id();
        PromiseVertex v = new PromiseVertex(Promise.as(id), Optional.empty(), graph);
        Map<String, Object> props = new HashMap<>();
        return new PromiseEdge(id, v, v, props, graph);
    }

    //region Fields
    private UniGraph graph;
    //endregion
}
