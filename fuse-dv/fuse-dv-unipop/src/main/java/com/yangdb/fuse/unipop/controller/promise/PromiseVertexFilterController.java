package com.yangdb.fuse.unipop.controller.promise;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import com.yangdb.fuse.unipop.controller.common.VertexControllerBase;
import com.yangdb.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.yangdb.fuse.unipop.controller.common.appender.FilterSourceSearchAppender;
import com.yangdb.fuse.unipop.controller.common.appender.MustFetchSourceSearchAppender;
import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.common.converter.ElementConverter;
import com.yangdb.fuse.unipop.controller.promise.appender.FilterIndexSearchAppender;
import com.yangdb.fuse.unipop.controller.promise.appender.FilterVerticesSearchAppender;
import com.yangdb.fuse.unipop.controller.promise.appender.PromiseConstraintSearchAppender;
import com.yangdb.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.yangdb.fuse.unipop.controller.promise.context.PromiseVertexFilterControllerContext;
import com.yangdb.fuse.unipop.controller.promise.converter.SearchHitPromiseFilterEdgeConverter;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.yangdb.fuse.unipop.converter.SearchHitScrollIterable;
import com.yangdb.fuse.unipop.predicates.SelectP;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.yangdb.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by Elad on 4/27/2017.
 * This controller handles constraints on the endB vertices of promise edges.
 * These constraints are modeled as constraints on special virtual 'promise-filter' edges.
 * The controller starts with promise-vertices, filter these vertices
 * and build promise-edges containing the result vertices as end vertices.
 */
public class PromiseVertexFilterController extends VertexControllerBase {

    //region Constructors
    public PromiseVertexFilterController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, SearchOrderProviderFactory orderProviderFactory) {
        super(labels -> Stream.ofAll(labels).size() == 1 &&
                Stream.ofAll(labels).get(0).equals(GlobalConstants.Labels.PROMISE_FILTER));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.orderProviderFactory = orderProviderFactory;
    }

    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if(constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        return filterPromiseVertices(searchVertexQuery, constraint, selectPHasContainers);
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> filterPromiseVertices(
            SearchVertexQuery searchVertexQuery,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers) {
        SearchBuilder searchBuilder = new SearchBuilder();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new PromiseVertexFilterControllerContext(
                        this.graph,
                        searchVertexQuery.getStepDescriptor(),
                        searchVertexQuery.getVertices(),
                        constraint,
                        selectPHasContainers,
                        schemaProvider,
                        searchVertexQuery.getLimit()));

        CompositeSearchAppender<CompositeControllerContext> appender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                    wrap(new FilterVerticesSearchAppender()),
                    wrap(new SizeSearchAppender(configuration)),
                    wrap(new PromiseConstraintSearchAppender()),
                    wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                    wrap(new FilterSourceSearchAppender()),
                    wrap(new FilterIndexSearchAppender()));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, GlobalConstants.INCLUDE_AGGREGATION).setSize(0);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                searchRequest,
                orderProviderFactory.build(context),
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime()
        );

        ElementConverter<SearchHit, Edge> converter = new SearchHitPromiseFilterEdgeConverter(graph);
        return Stream.ofAll(searchHits)
                .flatMap(converter::convert)
                .filter(Objects::nonNull).iterator();
    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private SearchOrderProviderFactory orderProviderFactory;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
