package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.converter.CompositeConverter;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;

/**
 * Created by liorp on 4/2/2017.
 */ //region EdgeController Implementation
class EdgeController implements SearchQuery.SearchController {
    EdgeController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, CompositeConverter converter) {}

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        return null;
    }
    //endregion
}
