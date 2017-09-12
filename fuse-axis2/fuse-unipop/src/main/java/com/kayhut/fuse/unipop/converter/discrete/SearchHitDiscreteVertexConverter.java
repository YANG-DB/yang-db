package com.kayhut.fuse.unipop.converter.discrete;

import com.kayhut.fuse.unipop.converter.ElementConverter;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class SearchHitDiscreteVertexConverter<E extends Element> implements ElementConverter<SearchHit, E> {
    //region Constructors
    public SearchHitDiscreteVertexConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public E convert(SearchHit searchHit) {
        return (E)new DiscreteVertex(searchHit.getId(), searchHit.getType(), this.graph, searchHit.sourceAsMap());
    }
    //endregion

    //region Fields
    private UniGraph graph;
    //endregion
}
