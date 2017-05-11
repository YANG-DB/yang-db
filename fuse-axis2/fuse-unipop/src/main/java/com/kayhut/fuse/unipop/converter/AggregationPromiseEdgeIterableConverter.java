package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.unipop.controller.utils.PromiseEdgeConstants;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.PromiseEdge;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
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

        Terms layer1 = (Terms) agg;

        layer1.getBuckets().forEach(b -> {
            String sourceId = b.getKeyAsString();
            PromiseVertex sourceVertex = new PromiseVertex(Promise.as(sourceId),Optional.empty(),graph);
            Terms layer2 = (Terms) b.getAggregations().asMap().get(PromiseEdgeConstants.DEST_AGGREGATION_LAYER);
            layer2.getBuckets().forEach(innerBucket -> {
                String destId = innerBucket.getKeyAsString();
                PromiseVertex destVertex = new PromiseVertex(Promise.as(destId), Optional.empty(), graph);
                Map<String,Object> propMap = new HashMap<>();
                propMap.put(PromiseEdgeConstants.PROMISE_EDGE_COUNT_PROP, innerBucket.getDocCount());
                PromiseEdge promiseEdge = new PromiseEdge(sourceVertex, destVertex, propMap, graph);
                edges.add(promiseEdge);
            });
        });

        return edges.iterator();

    }

    //region Fields
    private UniGraph graph;
    //endregion
}
