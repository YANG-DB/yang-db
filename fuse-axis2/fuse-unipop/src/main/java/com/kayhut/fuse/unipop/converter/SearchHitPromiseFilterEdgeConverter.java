package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.PromiseFilterEdge;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class SearchHitPromiseFilterEdgeConverter implements ElementConverter<SearchHit, Edge> {

    //region Constructor
    public SearchHitPromiseFilterEdgeConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    @Override
    public Edge convert(SearchHit hit) {
        PromiseVertex v = new PromiseVertex(Promise.as(hit.id()), Optional.empty(), graph);
        return new PromiseFilterEdge(v, graph);
    }

    //region Fields
    private UniGraph graph;
    //endregion
}
