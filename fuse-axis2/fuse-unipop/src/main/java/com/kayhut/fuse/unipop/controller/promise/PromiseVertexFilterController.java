package com.kayhut.fuse.unipop.controller.promise;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.VertexControllerBase;
import com.kayhut.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.ConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.FilterSourceSearchAppender;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexFilterControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.promise.appender.*;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.promise.converter.SearchHitPromiseFilterEdgeConverter;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.*;

/**
 * Created by Elad on 4/27/2017.
 * This controller handles constraints on the destination vertices of promise edges.
 * These constraints are modeled as constraints on special virtual 'promise-filter' edges.
 * The controller starts with promise-vertices, filter these vertices
 * and build promise-edges containing the result vertices as end vertices.
 */
public class PromiseVertexFilterController extends VertexControllerBase {

    //region Constructors
    public PromiseVertexFilterController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        super(Collections.singletonList(GlobalConstants.Labels.PROMISE_FILTER));

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
        Timer.Context time = metricRegistry.timer(name(PromiseVertexFilterController.class.getSimpleName(),"search")).time();
        if (Stream.ofAll(edgeLabels).isEmpty()) {
            return Collections.emptyIterator();
        }

        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices to start with");
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

        time.stop();
        return filterPromiseVertices(searchVertexQuery, constraint, selectPHasContainers);
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> filterPromiseVertices(
            SearchVertexQuery searchVertexQuery,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers) {
        Timer.Context time = metricRegistry.timer(name(PromiseVertexFilterController.class.getSimpleName(),"filterPromiseVertices")).time();

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseVertexFilterControllerContext context =
                new PromiseVertexFilterControllerContext(
                        this.graph,
                        searchVertexQuery.getVertices(),
                        constraint,
                        selectPHasContainers,
                        schemaProvider,
                        searchVertexQuery.getLimit());

        CompositeSearchAppender<PromiseVertexFilterControllerContext> appender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                    wrap(new FilterVerticesSearchAppender()),
                    wrap(new SizeSearchAppender(configuration)),
                    wrap(new ConstraintSearchAppender()),
                    wrap(new FilterSourceSearchAppender()),
                    wrap(new FilterIndexSearchAppender()));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.compose(client, true).setSearchType(SearchType.SCAN);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                metricRegistry, client,
                searchRequest,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());

        ElementConverter<SearchHit, Edge> converter = new SearchHitPromiseFilterEdgeConverter(graph);
        time.stop();
        return Stream.ofAll(searchHits)
                .map(converter::convert)
                .filter(Objects::nonNull).iterator();
    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private MetricRegistry metricRegistry;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
