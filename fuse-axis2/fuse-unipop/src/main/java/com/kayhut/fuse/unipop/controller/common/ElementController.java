package com.kayhut.fuse.unipop.controller.common;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.search.SearchQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by User on 19/03/2017.
 *
 * g.V() OR g.E() ==> edge controller
 */
public class ElementController implements SearchQuery.SearchController {
    //region Constructors
    public ElementController(
            SearchQuery.SearchController vertexController,
            SearchQuery.SearchController edgeController) {
        this.innerControllers = new HashMap<>();
        this.innerControllers.put(Vertex.class, vertexController);
        this.innerControllers.put(Edge.class, edgeController);
    }
    //endregion

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        Iterator<E> result = this.innerControllers.get(searchQuery.getReturnType()).search(searchQuery);
        return result;
    }
    //endregion

    //region Fields
    private Map<Class, SearchQuery.SearchController> innerControllers;
    //endregion
}
