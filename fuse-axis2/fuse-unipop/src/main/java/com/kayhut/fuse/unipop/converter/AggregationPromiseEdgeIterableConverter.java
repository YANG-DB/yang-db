package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.structure.PromiseEdge;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.unipop.structure.UniGraph;

import java.util.*;

/**
 * Created by r on 11/17/2015.
 */
public class AggregationPromiseEdgeIterableConverter implements ElementConverter<Aggregation, Iterator<Edge>> {
    //region Constructor
    public AggregationPromiseEdgeIterableConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion


    @Override
    public Iterator<Edge> convert(Aggregation agg) {

        ArrayList<Edge> edges = new ArrayList<>();

        StringTerms layer1 = (StringTerms) agg;

        layer1.getBuckets().forEach(b -> {
            String entAid = b.getKeyAsString();
            PromiseVertex sourceVertex = new PromiseVertex(new IdPromise(entAid),Optional.empty(),graph);
            StringTerms layer2 = (StringTerms) b.getAggregations().asMap().get("layer2");
            layer2.getBuckets().forEach(innerBucket -> {
                String entBid = innerBucket.getKeyAsString();
                PromiseVertex destVertex = new PromiseVertex(new IdPromise(entBid),Optional.empty(),graph);
                Map<String,Object> propMap = new HashMap<>();
                propMap.put(PromiseEdge.COUNT_PROP_KEY,innerBucket.getDocCount());
                PromiseEdge promiseEdge = new PromiseEdge(null,sourceVertex,destVertex,propMap,graph);
                edges.add(promiseEdge);
            });
        });

        return edges.iterator();

    }

    //region Fields
    private UniGraph graph;
    //endregion
}
