package com.kayhut.fuse.unipop.controller.promise.converter;

import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.idProvider.EdgeIdProvider;
import com.kayhut.fuse.unipop.controller.utils.labelProvider.LabelProvider;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.promise.PromiseEdge;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.unipop.structure.UniGraph;

import java.util.*;

/**
 * Created by r on 11/17/2015.
 */
public class AggregationPromiseEdgeIterableConverter implements ElementConverter<Map<String, Aggregation>, Iterator<Edge>> {
    //region Constructor
    public AggregationPromiseEdgeIterableConverter(
            UniGraph graph,
            EdgeIdProvider<String> edgeIdProvider,
            LabelProvider<String> vertexLabelProvider) {
        this.graph = graph;
        this.edgeIdProvider = edgeIdProvider;
        this.vertexLabelProvider = vertexLabelProvider;
    }
    //endregion


    @Override
    public Iterable<Iterator<Edge>> convert(Map<String, Aggregation> aggMap) {
        ArrayList<Edge> edges = new ArrayList<>();

        Terms layer1 = (Terms)aggMap.get(GlobalConstants.EdgeSchema.SOURCE);
        layer1.getBuckets().forEach(b -> {
            String sourceId = b.getKeyAsString();
            PromiseVertex sourceVertex = new PromiseVertex(Promise.as(sourceId),Optional.empty(),graph);

            Terms layer2 = (Terms) b.getAggregations().asMap().get(GlobalConstants.EdgeSchema.DEST);
            layer2.getBuckets().forEach(innerBucket -> {
                String destId = innerBucket.getKeyAsString();
                String destLabel = this.vertexLabelProvider.get(destId);

                PromiseVertex destVertex = new PromiseVertex(Promise.as(destId, destLabel), Optional.empty(), graph);

                Map<String,Object> propMap = new HashMap<>();
                propMap.put(GlobalConstants.HasKeys.COUNT, innerBucket.getDocCount());

                PromiseEdge promiseEdge = new PromiseEdge(
                        edgeIdProvider.get(
                                GlobalConstants.Labels.PROMISE,
                                sourceVertex,
                                destVertex,
                                propMap),
                        sourceVertex,
                        destVertex,
                        destVertex,
                        propMap,
                        graph);

                edges.add(promiseEdge);
            });
        });

        return Arrays.asList(edges.iterator());

    }

    //region Fields
    private UniGraph graph;
    private EdgeIdProvider<String> edgeIdProvider;
    private LabelProvider<String> vertexLabelProvider;
    //endregion
}
