package com.kayhut.fuse.unipop.controller;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by User on 19/03/2017.
 *
 * g.V() OR g.E() ==> edge controller
 */
public class PromiseElementController implements SearchQuery.SearchController {
    private final MetricRegistry metricRegistry;

    //region Constructors
    public PromiseElementController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider,MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.innerControllers = new HashMap<>();
        this.innerControllers.put(Vertex.class, new VertexController(client,configuration,graph,schemaProvider));
        this.innerControllers.put(Edge.class, new EdgeController(client,configuration,graph,schemaProvider));
    }
    //endregion

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        Timer.Context time = metricRegistry.timer(name(PromiseElementController.class.getSimpleName(),"search")).time();
        Iterator<E> result = this.innerControllers.get(searchQuery.getReturnType()).search(searchQuery);
        time.stop();
        return result;
    }
    //endregion

    //region Fields
    private Map<Class, SearchQuery.SearchController> innerControllers;
    //endregion
}
