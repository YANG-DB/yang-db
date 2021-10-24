package com.yangdb.fuse.unipop.controller.discrete;

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

import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import com.yangdb.fuse.unipop.controller.common.VertexControllerBase;
import com.yangdb.fuse.unipop.controller.common.appender.*;
import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.common.converter.ElementConverter;
import com.yangdb.fuse.unipop.controller.discrete.context.DiscreteVertexFilterControllerContext;
import com.yangdb.fuse.unipop.controller.discrete.converter.DiscreteVertexFilterConverter;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.yangdb.fuse.unipop.converter.SearchHitScrollIterable;
import com.yangdb.fuse.unipop.predicates.SelectP;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.search.SearchHit;
import org.unipop.process.predicate.DistinctFilterP;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.yangdb.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by roman.margolis on 26/09/2017.
 * This Search controller is responsible for the edge->vertex (including filter) type documents search (arriving from edge to vertex) pushed down (including predicates) to the engine...
 */
public class DiscreteVertexFilterController extends VertexControllerBase {
    //region Constructors
    public DiscreteVertexFilterController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, SearchOrderProviderFactory orderProviderFactory) {
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

        return filterVertices(searchVertexQuery, constraint, selectPHasContainers);
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> filterVertices(
            SearchVertexQuery searchVertexQuery,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers) {
        SearchBuilder searchBuilder = new SearchBuilder();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexFilterControllerContext(
                        this.graph,
                        searchVertexQuery.getStepDescriptor(),
                        searchVertexQuery.getVertices(),
                        constraint,
                        selectPHasContainers,
                        schemaProvider,
                        searchVertexQuery.getLimit()));

        CompositeSearchAppender<CompositeControllerContext> appender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new SizeSearchAppender(configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new FilterBulkSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new FilterRoutingSearchAppender()),
                        wrap(new FilterIndexSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, GlobalConstants.INCLUDE_AGGREGATION);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                searchRequest,
                orderProviderFactory.build(context),
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime()
        );

        //log step controller query
        context.getStepDescriptor().getDescription().ifPresent(v->
                profiler.get().setAnnotation(v,searchRequest.toString()));
        //convert hits to elements
        ElementConverter<SearchHit, Edge> converter = new DiscreteVertexFilterConverter(context,profiler);

        javaslang.collection.Iterator<Edge> results = Stream.ofAll(searchHits)
                .flatMap(converter::convert)
                .filter(Objects::nonNull).iterator();

        //dedupe for distinct operator
        if (context.getConstraint().isPresent()) {
            if (!Stream.ofAll(new TraversalHasStepFinder(DistinctFilterP::hasDistinct)
                    .getValue(context.getConstraint().get().getTraversal()))
                    .toJavaSet().isEmpty()) {
                results = results.distinct();
            }
        }
        return results;
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
