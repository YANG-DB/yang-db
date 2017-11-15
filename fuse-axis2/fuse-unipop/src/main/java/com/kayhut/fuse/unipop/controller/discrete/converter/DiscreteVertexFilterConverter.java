package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import com.kayhut.fuse.unipop.structure.promise.PromiseFilterEdge;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Roman on 11/15/2017.
 */
public class DiscreteVertexFilterConverter implements ElementConverter<SearchHit, Edge> {
    //region Constructor
    public DiscreteVertexFilterConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<Edge> convert(SearchHit hit) {
        DiscreteVertex v = new DiscreteVertex(hit.id(), hit.getType(), graph, hit.sourceAsMap());

        return Collections.singletonList(new DiscreteEdge(
                v.id(),
                GlobalConstants.Labels.PROMISE_FILTER,
                v,
                v,
                graph,
                Collections.emptyMap()));
    }
    //endregion

    //region Fields
    private UniGraph graph;
    //endregion
}
