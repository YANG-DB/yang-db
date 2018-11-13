package com.kayhut.fuse.unipop.controller.promise.converter;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
 * Created by roman on 11/17/2015.
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
            String sourceLabel = this.vertexLabelProvider.get(sourceId);
            PromiseVertex sourceVertex = new PromiseVertex(Promise.as(sourceId, sourceLabel),Optional.empty(),graph);

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
