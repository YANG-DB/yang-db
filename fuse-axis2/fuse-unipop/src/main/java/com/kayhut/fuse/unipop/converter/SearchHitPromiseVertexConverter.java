package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
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
        return new PromiseVertex(new IdPromise(element.id()), Optional.empty(), graph);
    }
    //endregion

    //region Fields
    private UniGraph graph;
    //endregion
}
