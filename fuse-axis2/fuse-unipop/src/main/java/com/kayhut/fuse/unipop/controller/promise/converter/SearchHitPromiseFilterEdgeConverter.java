package com.kayhut.fuse.unipop.controller.promise.converter;

import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.promise.PromiseFilterEdge;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

import java.util.Map;
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
        Map<String, Object> propertiesMap = hit.sourceAsMap();
        PromiseVertex v = new PromiseVertex(Promise.as(hit.id(), hit.getType()), Optional.empty(), graph, propertiesMap);

        return new PromiseFilterEdge(v, graph);
    }

    //region Fields
    private UniGraph graph;
    //endregion
}
