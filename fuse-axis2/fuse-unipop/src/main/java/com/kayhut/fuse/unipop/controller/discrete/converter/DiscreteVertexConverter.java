package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.search.SearchHit;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteVertexConverter<E extends Element> implements ElementConverter<SearchHit, E> {
    //region Constructors
    public DiscreteVertexConverter(ElementControllerContext context) {
        this.context = context;
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public E convert(SearchHit searchHit) {
        return (E)new DiscreteVertex(searchHit.getId(), searchHit.getType(), context.getGraph(), searchHit.sourceAsMap());
    }
    //endregion

    //region Fields
    private ElementControllerContext context;
    //endregion
}
