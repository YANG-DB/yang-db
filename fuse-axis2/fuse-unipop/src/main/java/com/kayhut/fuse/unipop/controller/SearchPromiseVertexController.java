package com.kayhut.fuse.unipop.controller;

import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.converter.PromiseEdgeConverter;
import com.kayhut.fuse.unipop.converter.SearchHitPromiseVertexConverter;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
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

        HasContainer edgeConstraint = constraintHasContainers.get(0);

        Traversal startVerticesConstraint = buildStartVerticesConstraint(searchVertexQuery.getVertices());

        return queryPromiseEdges(startVerticesConstraint, edgeConstraint);

    }
    //endregion

    //region Private Methods
    private Iterator<Edge> queryPromiseEdges(Traversal startVerticesConstraint, HasContainer edgeConstraint) {

        SearchBuilder searchBuilder = new SearchBuilder();

        //append start vertices constraint
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(
                searchBuilder.getQueryBuilder().seekRoot().query().filtered().filter().bool("root_bool").must(), true);
        traversalQueryTranslator.visit(startVerticesConstraint);

        //append edges constraint to query
        TraversalConstraint traversalConstraint = (TraversalConstraint)edgeConstraint.getValue();

        //append aggregations ?

        //search
        /*SearchRequestBuilder searchRequest = searchBuilder.compose(client, false);
        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                searchRequest,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime());
*/

        //convert result
        // return convert(searchHits, new PromiseEdgeConverter(graph));

        return Collections.emptyIterator();
    }

    private Iterator<Edge> convert(SearchHitScrollIterable searchHits, PromiseEdgeConverter promiseEdgeConverter) {
        //TODO: change PromiseEdgeConverter to return Edge?
        return Stream.ofAll(searchHits)
                .map(hit -> (Edge)promiseEdgeConverter.convert(hit))
                .filter(Objects::nonNull).iterator();
    }

    private Traversal buildStartVerticesConstraint(List<Vertex> vertices) {
       return __.has("id", P.within(vertices.stream().map(vertex -> vertex.id()).collect(Collectors.toList())));
    }

    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
