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

import com.yangdb.fuse.unipop.controller.OpensearchGraphConfiguration;
import com.yangdb.fuse.unipop.controller.common.VertexControllerBase;
import com.yangdb.fuse.unipop.controller.common.appender.*;
import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.common.converter.*;
import com.yangdb.fuse.unipop.controller.discrete.appender.DualEdgeDirectionSearchAppender;
import com.yangdb.fuse.unipop.controller.discrete.context.DiscreteVertexControllerContext;
import com.yangdb.fuse.unipop.controller.discrete.converter.DiscreteEdgeConverter;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.converter.SearchHitScrollIterable;
import com.yangdb.fuse.unipop.predicates.SelectP;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.unipop.process.predicate.DistinctFilterP;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by roman.margolis on 13/09/2017.
 * This Search controller is responsible for the Edge type documents search pushed down to the engine...
 */
public class DiscreteVertexController extends VertexControllerBase {
    //region Constructors
    public DiscreteVertexController(Client client, OpensearchGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, SearchOrderProviderFactory orderProviderFactory) {
        super(labels -> Stream.ofAll(labels).isEmpty() ||
                Stream.ofAll(schemaProvider.getEdgeLabels()).toJavaSet().containsAll(Stream.ofAll(labels).toJavaSet()),
                Stream.ofAll(schemaProvider.getEdgeLabels()).toJavaSet());

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
        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey()
                        .equalsIgnoreCase(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();

        if (constraintHasContainers.size() > 1) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if (constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        if (!Stream.ofAll(edgeLabels).isEmpty()) {
            constraint = constraint.isPresent() ?
                    Optional.of(Constraint.by(__.and(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList())), constraint.get().getTraversal()))) :
                    Optional.of(Constraint.by(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList()))));
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexControllerContext(
                        this.graph,
                        searchVertexQuery.getStepDescriptor(),
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        searchVertexQuery.getLimit(),
                        searchVertexQuery.getDirection(),
                        searchVertexQuery.getVertices()));

        //skip vertex query since needed properties exist on already fetched vertices
        if (canDoWithoutQuery(searchVertexQuery, context)) {
            ElementConverter<DataItem, Edge> elementConverter = new CompositeElementConverter<>(
                    new DiscreteEdgeConverter<>(context, profiler));

            javaslang.collection.Iterator<Edge> results = Stream.ofAll(searchVertexQuery.getVertices())
                    .map(VertexDataItem::new)
                    .flatMap(elementConverter::convert)
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

        CompositeSearchAppender<CompositeControllerContext> searchAppender = getAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        searchAppender.append(searchBuilder, context);

        //if aggregation has content - first perform aggregation and the results would be down streamed to the next step which is the search actual hit
        if(searchBuilder.getAggregationBuilder().getAggregations().iterator().hasNext()) {
            context = filterByAggregation(context, searchBuilder, searchVertexQuery);
            //override former appenders with new agg filter results (new context)
            searchAppender = getAppender();
            searchBuilder = new SearchBuilder();
            searchAppender.append(searchBuilder, context);
        }

        //search
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
        ElementConverter<DataItem, Edge> elementConverter = new CompositeElementConverter<>(
                new DiscreteEdgeConverter<>(context, this.profiler));

        javaslang.collection.Iterator<Edge> results = Stream.ofAll(searchHits)
                .map(SearchHitDataItem::new)
                .flatMap(elementConverter::convert)
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

    private CompositeSearchAppender<CompositeControllerContext> getAppender() {
        return new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                wrap(new IndexSearchAppender()),
                wrap(new SizeSearchAppender(this.configuration)),
                wrap(new ConstraintSearchAppender()),
                wrap(new FilterSourceSearchAppender()),
                wrap(new FilterSourceRoutingSearchAppender()),
                wrap(new ElementRoutingSearchAppender()),
                wrap(new EdgeBulkSearchAppender()),
                wrap(new EdgeSourceSearchAppender()),
                //todo: add configuration to enable/disable routing
//                        wrap(new EdgeRoutingSearchAppender()),
                wrap(new EdgeSourceRoutingSearchAppender()),
                wrap(new EdgeIndexSearchAppender()),
                wrap(new DualEdgeDirectionSearchAppender()),
                wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                wrap(new NormalizeRoutingSearchAppender(50)),
                wrap(new NormalizeIndexSearchAppender(100)));
    }

    /**
     * this section is done separately against the E/S engine since we cant use the resulting aggregation results to get document (using filter) inside the same query -
     * this feature was requested form E/S in the pull requests and still awaiting development - once completed this method will be unified with the calling thread
     * @param context
     * @param searchBuilder
     * @param searchVertexQuery
     * @return
     */
    private CompositeControllerContext.Impl filterByAggregation(CompositeControllerContext context, SearchBuilder searchBuilder, SearchVertexQuery searchVertexQuery) {
        searchBuilder.setLimit(0);//agg needs no actual hits returned only the agg buckets themselves
        searchBuilder.setSize(context.getBulkSize());//set size for aggregation max buckets (according to bulk size)
        SearchRequestBuilder searchRequest = searchBuilder.build(client, true);
        //execute agg query
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        //log step controller query
        context.getStepDescriptor().getDescription().ifPresent(v->
                profiler.get().setAnnotation(v,searchRequest.toString()));

        List aggDataItems = SearchAggDataItem.build(searchResponse.getAggregations().getAsMap())
                .stream().map(SearchAggDataItem::id).collect(Collectors.toList());

        List<Vertex> filteredEdges = StreamSupport.stream(context.getBulkVertices().spliterator(), false)
                .filter(e -> aggDataItems.contains(e.id()))
                .collect(Collectors.toList());

        return new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexControllerContext(
                        this.graph,
                        searchVertexQuery.getStepDescriptor(),
                        this.schemaProvider,
                        context.getConstraint(),
                        context.getSelectPHasContainers(),
                        searchVertexQuery.getLimit(),
                        searchVertexQuery.getDirection(),
                        filteredEdges));
    }


    //endregion

    //region Private Methods
    private boolean canDoWithoutQuery(SearchVertexQuery searchVertexQuery, CompositeControllerContext context) {
        Set<String> labels = context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        if (Stream.ofAll(labels).size() == 1) {
            String edgeLabel = Stream.ofAll(labels).get(0);

            //currently assuming same vertex in bulk
            String vertexLabel = searchVertexQuery.getVertices().get(0).label();
            Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(vertexLabel, searchVertexQuery.getDirection(), edgeLabel);

            if (Stream.ofAll(edgeSchemas).size() == 0) {
                return true;
            }

            //currently assuming a single relevant edge schema
            GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

            if (Stream.ofAll(edgeSchema.getEndB().get().getIdFields()).size() == 1) {
                String idField = Stream.ofAll(edgeSchema.getEndB().get().getIdFields()).get(0);

                if (idField.equals(GlobalConstants._ID)) {
                    return false;
                }

                VertexProperty<String> idProperty = searchVertexQuery.getVertices().get(0).property(idField);
                if (idProperty == VertexProperty.<String>empty()) {
                    return false;
                }
            } else {
                return false;
            }

            return true;
        }

        return false;
    }
    //endregion

    //region Fields
    private Client client;
    private OpensearchGraphConfiguration configuration;
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private SearchOrderProviderFactory orderProviderFactory;
    //endregion
}
