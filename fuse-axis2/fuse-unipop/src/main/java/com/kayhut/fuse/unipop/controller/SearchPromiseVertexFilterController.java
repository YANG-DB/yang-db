package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.controller.context.PromiseVertexFilterControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.EdgeConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.FilterVerticesSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.SizeSearchAppender;
import com.kayhut.fuse.unipop.converter.SearchHitPromiseFilterEdgeConverter;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Elad on 4/27/2017.
 * This controller handles constraints on the destination vertices of promise edges.
 * These constraints are modeled as constraints on special virtual 'promise-filter' edges.
 * The controller starts with promise-vertices, filter these vertices
 * and build promise-edges containing the result vertices as end vertices.
 */
public class SearchPromiseVertexFilterController implements SearchVertexQuery.SearchVertexController {

    //region Constructors
    public SearchPromiseVertexFilterController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider) {
        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }

    //endregion
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {

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

        return filterPromiseVertices(searchVertexQuery.getVertices(), constraint, searchVertexQuery);

    }

    private Iterator<Edge> filterPromiseVertices(List<Vertex> vertices, Optional<TraversalConstraint> constraint, SearchVertexQuery searchVertexQuery) {

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseVertexFilterControllerContext context = new PromiseVertexFilterControllerContext(vertices,
                                                                                                constraint,
                                                                                                schemaProvider,
                                                                                                searchVertexQuery);

        CompositeSearchAppender appender = new CompositeSearchAppender(CompositeSearchAppender.Mode.all,
                new FilterVerticesSearchAppender(),
                new EdgeConstraintSearchAppender(),
                new SizeSearchAppender(configuration));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.compose(client, true).setSearchType(SearchType.SCAN);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                searchRequest,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());

        return convert(searchHits, new SearchHitPromiseFilterEdgeConverter(graph));
    }

    private Iterator<Edge> convert(SearchHitScrollIterable searchHits, SearchHitPromiseFilterEdgeConverter converter) {
        return Stream.ofAll(searchHits)
                .map(hit -> converter.convert(hit))
                .filter(Objects::nonNull).iterator();
    }

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
