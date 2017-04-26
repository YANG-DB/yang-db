package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.appender.EdgeConstraintSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.PromiseEdgeAggregationAppender;
import com.kayhut.fuse.unipop.controller.search.appender.StartVerticesSearchAppender;
import com.kayhut.fuse.unipop.controller.utils.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.converter.AggregationPromiseEdgeIterableConverter;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
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
import java.util.stream.Collectors;

/**
 * Created by User on 16/03/2017.
 */
public class SearchPromiseVertexController implements SearchVertexQuery.SearchVertexController {

    //region Constructors
    public SearchPromiseVertexController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider) {
        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region SearchVertexQuery.SearchVertexController Implementation
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

        return queryPromiseEdges(searchVertexQuery.getVertices(), constraintHasContainers);

    }
    //endregion

    //region Private Methods
    private Iterator<Edge> queryPromiseEdges(List<Vertex> startVertices, List<HasContainer> edgeConstraints) {

        Optional<TraversalConstraint> constraint = edgeConstraints.stream()
                                                                  .findFirst()
                                                                  .filter(hasContainer ->
                                                                          hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                                                                  .map(h -> (TraversalConstraint) h.getValue());

        SearchBuilder searchBuilder = new SearchBuilder();

        PromiseVertexControllerContext context = new PromiseVertexControllerContext(startVertices,constraint,schemaProvider);

        //append start vertices constraint
        StartVerticesSearchAppender startVerticesAppender = new StartVerticesSearchAppender();
        startVerticesAppender.append(searchBuilder, context);

        //append edges constraint to query
        EdgeConstraintSearchAppender edgeConstraintAppender = new EdgeConstraintSearchAppender();
        edgeConstraintAppender.append(searchBuilder, context);

        //build aggregations
        PromiseEdgeAggregationAppender aggregationAppender = new PromiseEdgeAggregationAppender();
        aggregationAppender.append(searchBuilder, context);

        //search
        SearchRequestBuilder searchRequest = searchBuilder.compose(client, true).setSearchType(SearchType.COUNT);

        SearchResponse response = searchRequest.execute().actionGet();

        //convert result
        return convert(response);

    }

    private Iterator<Edge> convert(SearchResponse response) {

        if( response == null ) {
            return Collections.emptyIterator();
        }

        Aggregation agg = response.getAggregations().asMap().get("layer1");

        AggregationPromiseEdgeIterableConverter converter = new AggregationPromiseEdgeIterableConverter(graph);

        return converter.convert(agg);

    }

    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
