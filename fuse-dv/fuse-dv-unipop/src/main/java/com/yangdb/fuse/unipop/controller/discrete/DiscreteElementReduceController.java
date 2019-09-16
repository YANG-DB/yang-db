package com.yangdb.fuse.unipop.controller.discrete;

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
import com.yangdb.fuse.unipop.controller.common.appender.*;
import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.discrete.appender.DualEdgeDirectionSearchAppender;
import com.yangdb.fuse.unipop.controller.discrete.context.DiscreteElementControllerContext;
import com.yangdb.fuse.unipop.controller.discrete.context.DiscreteVertexControllerContext;
import com.yangdb.fuse.unipop.controller.promise.GlobalConstants;
import com.yangdb.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.utils.CollectionUtil;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.predicates.SelectP;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.structure.ElementType;
import com.yangdb.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.unipop.query.aggregation.ReduceEdgeQuery;
import org.unipop.query.aggregation.ReduceQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by Roman on 3/14/2018.
 */
public class DiscreteElementReduceController implements ReduceQuery.SearchController {
    //region Constructors
    public DiscreteElementReduceController(
            Client client,
            ElasticGraphConfiguration configuration,
            UniGraph graph,
            GraphElementSchemaProvider schemaProvider) {

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region ReduceQuery.SearchController Implementation
    @Override
    public long count(ReduceQuery reduceQuery) {
        SearchBuilder searchBuilder = new SearchBuilder();
        if(reduceQuery instanceof ReduceEdgeQuery){
            buildEdgeQuery((ReduceEdgeQuery) reduceQuery, searchBuilder);
        }else{
            buildVertexQuery(reduceQuery, searchBuilder);
        }

        SearchRequestBuilder searchRequest = searchBuilder.build(client, false);
        SearchResponse response = searchRequest.execute().actionGet();
        return response.getHits().getTotalHits().value;

    }

    @Override
    public long max(ReduceQuery uniQuery) {
        throw new UnsupportedOperationException("Please implement me ...");
    }

    @Override
    public long avg(ReduceQuery uniQuery) {
        throw new UnsupportedOperationException("Please implement me ...");
    }

    public long min(ReduceQuery uniQuery) {
        throw new UnsupportedOperationException("Please implement me ...");
    }

    private void buildEdgeQuery(ReduceEdgeQuery reduceQuery, SearchBuilder searchBuilder) {
        Iterable<String> edgeLabels = getRequestedEdgeLabels(reduceQuery.getPredicates().getPredicates());

        List<HasContainer> constraintHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }


        List<HasContainer> selectPHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        if (!Stream.ofAll(edgeLabels).isEmpty()) {
            constraint = constraint.isPresent() ?
                    Optional.of(Constraint.by(__.and(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList())), constraint.get().getTraversal()))) :
                    Optional.of(Constraint.by(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList()))));
        }

        List<HasContainer> vertexHasContainer = Stream.ofAll(reduceQuery.getVertexPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if(vertexHasContainer.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        TraversalConstraint vertexTraversalConstraint = (TraversalConstraint)vertexHasContainer.get(0).getValue();
        String vertexLabel = Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(vertexTraversalConstraint.getTraversal(), T.label.getAccessor())).get();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexControllerContext(
                        this.graph,
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        1,
                        reduceQuery.getDirection(),
                        Collections.singleton(new DiscreteVertex(1,vertexLabel, graph, new HashMap<>()))));

        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new IndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new EdgeSourceSearchAppender()),
                        wrap(new EdgeRoutingSearchAppender()),
                        wrap(new EdgeSourceRoutingSearchAppender()),
                        wrap(new EdgeIndexSearchAppender()),
                        wrap(new DualEdgeDirectionSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender("type")),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        searchAppender.append(searchBuilder, context);
    }

    private void buildVertexQuery(ReduceQuery reduceQuery, SearchBuilder searchBuilder) {
        List<HasContainer> constraintHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                new DiscreteElementControllerContext(this.graph,
                        ElementType.vertex,
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        0),
                null);

        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new ElementIndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender("type")),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        searchAppender.append(searchBuilder, context);
    }

    protected Iterable<String> getRequestedEdgeLabels(Iterable<HasContainer> hasContainers) {
        Optional<HasContainer> labelHasContainer =
                Stream.ofAll(hasContainers)
                        .filter(hasContainer -> hasContainer.getKey().equals(T.label.getAccessor()))
                        .toJavaOptional();

        if (!labelHasContainer.isPresent()) {
            return Collections.emptyList();
        }

        List<String> requestedEdgeLabels = CollectionUtil.listFromObjectValue(labelHasContainer.get().getValue());
        return requestedEdgeLabels;
    }
    //endregion

    //region Fields
    private Client client;
    private ElasticGraphConfiguration configuration;
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    //endregion
}
