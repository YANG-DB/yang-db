package com.kayhut.fuse.unipop.controller.promise;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.VertexControllerBase;
import com.kayhut.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.ConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.promise.appender.*;
import com.kayhut.fuse.unipop.controller.utils.idProvider.PromiseEdgeIdProvider;
import com.kayhut.fuse.unipop.controller.utils.labelProvider.PrefixedLabelProvider;
import com.kayhut.fuse.unipop.controller.promise.converter.AggregationPromiseEdgeIterableConverter;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static com.codahale.metrics.Timer.Context;
import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by User on 16/03/2017.
 */
public class PromiseVertexController extends VertexControllerBase {

    //region Constructors
    public PromiseVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        super(Collections.singletonList(GlobalConstants.Labels.PROMISE));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        Context time = metricRegistry.timer(name(PromiseVertexController.class.getSimpleName(),"search")).time();
        if (Stream.ofAll(edgeLabels).isEmpty()) {
            return Collections.emptyIterator();
        }

        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices to start with");
        }

        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey()
                        .toLowerCase()
                        .equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();

        if (constraintHasContainers.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if(constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        try {
            return queryPromiseEdges(searchVertexQuery.getVertices(), constraint);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyIterator();
        } finally {
            time.stop();
        }
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> queryPromiseEdges(List<Vertex> startVertices, Optional<TraversalConstraint> constraint) throws Exception {
        Context time = metricRegistry.timer(name(PromiseVertexController.class.getSimpleName(), "queryPromiseEdges")).time();
        Timer timeEs = metricRegistry.timer(name(PromiseVertexController.class.getSimpleName(),"queryPromiseEdges:elastic"));
        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseVertexControllerContext context = new PromiseVertexControllerContext(graph, schemaProvider, constraint, Collections.emptyList(), 0, startVertices);

        CompositeSearchAppender<PromiseVertexControllerContext> compositeAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new StartVerticesSearchAppender()),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new PromiseEdgeAggregationAppender()),
                        wrap(new PromiseEdgeIndexAppender()));

        compositeAppender.append(searchBuilder, context);

        if(searchBuilder.getIndices().size() == 0) {
            //there is no relevant index to search...
            return Collections.emptyIterator();
        }

        //search
        SearchRequestBuilder searchRequest = searchBuilder.compose(client, true).setSearchType(SearchType.COUNT);

        SearchResponse response = searchRequest.execute().actionGet();

        //convert result
        AggregationPromiseEdgeIterableConverter converter = new AggregationPromiseEdgeIterableConverter(
                graph,
                new PromiseEdgeIdProvider(constraint),
                new PrefixedLabelProvider("_"));

        //timeEs es search took in ms
        timeEs.update(response.getTookInMillis(), TimeUnit.MILLISECONDS);
        time.stop();
        return converter.convert(response.getAggregations().asMap());

    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private ElasticGraphConfiguration configuration;
    private MetricRegistry metricRegistry;
    //endregion
}
