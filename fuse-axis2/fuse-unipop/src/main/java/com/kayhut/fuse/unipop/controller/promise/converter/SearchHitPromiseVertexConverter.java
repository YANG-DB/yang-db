package com.kayhut.fuse.unipop.controller.promise.converter;

import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by r on 11/17/2015.
 */
public class SearchHitPromiseVertexConverter implements ElementConverter<SearchHit, Element> {
    //region Constructor
    public SearchHitPromiseVertexConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    @Override
    public Element convert(SearchHit element) {
        return new PromiseVertex(Promise.as(element.id(), element.getType()), Optional.empty(), graph, element.sourceAsMap());
    }
    //endregion

    //region Fields
    private UniGraph graph;
    //endregion
}
