package com.kayhut.fuse.unipop.controller.promise;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.VertexControllerBase;
import com.kayhut.fuse.unipop.controller.common.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.FilterSourceSearchAppender;
import com.kayhut.fuse.unipop.controller.common.appender.MustFetchSourceSearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.promise.appender.FilterIndexSearchAppender;
import com.kayhut.fuse.unipop.controller.promise.appender.FilterVerticesSearchAppender;
import com.kayhut.fuse.unipop.controller.promise.appender.PromiseConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexFilterControllerContext;
import com.kayhut.fuse.unipop.controller.promise.converter.SearchHitPromiseFilterEdgeConverter;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
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

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by Elad on 4/27/2017.
 * This controller handles constraints on the destination vertices of promise edges.
 * These constraints are modeled as constraints on special virtual 'promise-filter' edges.
 * The controller starts with promise-vertices, filter these vertices
 * and build promise-edges containing the result vertices as end vertices.
 */
public class PromiseVertexFilterController extends VertexControllerBase {

    //region Constructors
    public PromiseVertexFilterController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider) {
        super(labels -> Stream.ofAll(labels).size() == 1 &&
                Stream.ofAll(labels).get(0).equals(GlobalConstants.Labels.PROMISE_FILTER));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
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
                    wrap(new MustFetchSourceSearchAppender("type")),
                    wrap(new FilterSourceSearchAppender()),
                    wrap(new FilterIndexSearchAppender()));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, true).setSize(0);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                searchRequest,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());

        ElementConverter<SearchHit, Edge> converter = new SearchHitPromiseFilterEdgeConverter(graph);
        return Stream.ofAll(searchHits)
                .flatMap(converter::convert)
                .filter(Objects::nonNull).iterator();
    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
