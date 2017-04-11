package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.structure.PromiseEdge;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by r on 11/17/2015.
 */
public class PromiseEdgeConverter implements ElementConverter<SearchHit, Element> {
    //region Constructor
    public PromiseEdgeConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion


    @Override
    public Element convert(SearchHit element) {
        Edge edge = (Edge)element;
        Vertex outVertex = edge.outVertex();
        Vertex inVertex = edge.inVertex();

        return new PromiseEdge(
                        element.id(),
                        new PromiseVertex(new IdPromise(outVertex.id()), Optional.empty(), this.graph),
                        new PromiseVertex(new IdPromise(inVertex.id()), Optional.empty(), this.graph),
                        null,
                        this.graph);
    }

    //region Fields
    private UniGraph graph;
    //endregion
}
