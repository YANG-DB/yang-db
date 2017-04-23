package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by User on 16/03/2017.
 */
public class SearchPromiseVertexController implements SearchVertexQuery.SearchVertexController {
    public SearchPromiseVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider) {

    }

    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        return Collections.emptyIterator();
    }
    //endregion
}
