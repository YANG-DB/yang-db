package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.appender.*;
import com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil;
import com.kayhut.fuse.unipop.controller.utils.idProvider.PromiseEdgeIdProvider;
import com.kayhut.fuse.unipop.controller.utils.labelProvider.PrefixedLabelProvider;
import com.kayhut.fuse.unipop.converter.AggregationPromiseEdgeIterableConverter;
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
import org.elasticsearch.search.aggregations.Aggregation;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.*;

/**
 * Created by User on 16/03/2017.
 */
public class PromiseVertexController extends PromiseVertexControllerBase {

    //region Constructors
    public PromiseVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider) {
        super(Collections.singletonList(GlobalConstants.Labels.PROMISE));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region PromiseVertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
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
        }
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> queryPromiseEdges(List<Vertex> startVertices, Optional<TraversalConstraint> constraint) throws Exception {

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseVertexControllerContext context = new PromiseVertexControllerContext(startVertices, constraint, schemaProvider);

        CompositeSearchAppender<PromiseVertexControllerContext> compositeAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new StartVerticesSearchAppender()),
                        wrap(new EdgeConstraintSearchAppender()),
                        wrap(new PromiseEdgeAggregationAppender()),
                        wrap(new PromiseEdgeIndexAppender()));

        compositeAppender.append(searchBuilder, context);

        //search
        SearchRequestBuilder searchRequest = searchBuilder.compose(client, true).setSearchType(SearchType.COUNT);

        SearchResponse response = searchRequest.execute().actionGet();

        //convert result
        AggregationPromiseEdgeIterableConverter converter = new AggregationPromiseEdgeIterableConverter(
                graph,
                new PromiseEdgeIdProvider(constraint),
                new PrefixedLabelProvider("_"));
        return converter.convert(response.getAggregations().asMap());

    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
