package com.yangdb.fuse.unipop.controller.promise;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import com.yangdb.fuse.unipop.controller.common.VertexControllerBase;
import com.yangdb.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.promise.appender.PromiseConstraintSearchAppender;
import com.yangdb.fuse.unipop.controller.promise.appender.PromiseEdgeAggregationAppender;
import com.yangdb.fuse.unipop.controller.promise.appender.PromiseEdgeIndexAppender;
import com.yangdb.fuse.unipop.controller.promise.appender.StartVerticesSearchAppender;
import com.yangdb.fuse.unipop.controller.promise.context.PromiseVertexControllerContext;
import com.yangdb.fuse.unipop.controller.promise.converter.AggregationPromiseEdgeIterableConverter;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.utils.idProvider.HashEdgeIdProvider;
import com.yangdb.fuse.unipop.controller.utils.labelProvider.PrefixedLabelProvider;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by lior.perry on 16/03/2017.
 */
public class PromiseVertexController extends VertexControllerBase {

    //region Constructors
    public PromiseVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider) {
        super(labels -> Stream.ofAll(labels).size() == 1 &&
                Stream.ofAll(labels).get(0).equals(GlobalConstants.Labels.PROMISE));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (searchVertexQuery.getVertices().size() == 0) {
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey()
                        .toLowerCase()
                        .equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();

        if (constraintHasContainers.size() > 1) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if (constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        try {
            return queryPromiseEdges(searchVertexQuery.getVertices(), constraint);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyIterator();
        }
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> queryPromiseEdges(List<Vertex> startVertices, Optional<TraversalConstraint> constraint) throws Exception {
        SearchBuilder searchBuilder = new SearchBuilder();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new PromiseVertexControllerContext(
                        graph,
                        schemaProvider,
                        constraint,
                        Collections.emptyList(),
                        0,
                        startVertices));

        CompositeSearchAppender<CompositeControllerContext> compositeAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new StartVerticesSearchAppender()),
                        wrap(new PromiseConstraintSearchAppender()),
                        wrap(new PromiseEdgeAggregationAppender()),
                        wrap(new PromiseEdgeIndexAppender()));

        compositeAppender.append(searchBuilder, context);

        if (searchBuilder.getIndices().size() == 0) {
            //there is no relevant index getTo search...
            return Collections.emptyIterator();
        }

        //search
        SearchRequestBuilder searchRequest = searchBuilder.build(client, true).setSize(0);
        SearchResponse response = searchRequest.execute().actionGet();

        //convert result
        AggregationPromiseEdgeIterableConverter converter = new AggregationPromiseEdgeIterableConverter(
                graph,
                new HashEdgeIdProvider(constraint),
                new PrefixedLabelProvider("_"));

        return Stream.ofAll(converter.convert(response.getAggregations().asMap())).flatMap(edgeIterator -> () -> edgeIterator).iterator();

    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
