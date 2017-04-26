package com.kayhut.fuse.unipop;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseElementController;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexController;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Any;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchQuery;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 23/04/2017.
 */
public class SearchPromiseVertexControllerTest {
    Client client;
    ElasticGraphConfiguration configuration;

    @Before
    public void setUp() throws Exception {


        client = mock(Client.class);

        SearchResponse responseMock = mock(SearchResponse.class);

        Terms.Bucket term1 = mock(Terms.Bucket.class);
        when(term1.getKey()).thenReturn("1");
        when(term1.getDocCount()).thenReturn(1000L);

        Terms layer1Terms = mock(Terms.class);
        when(layer1Terms.getBuckets()).thenReturn(Arrays.asList(term1));

        Aggregations aggregations = mock(Aggregations.class);
        when(aggregations.asList()).thenReturn(Arrays.asList(layer1Terms));

        when(responseMock.getAggregations()).thenReturn(aggregations);

        ListenableActionFuture<SearchResponse> futureMock = mock(ListenableActionFuture.class);
        when(futureMock.get()).thenReturn(responseMock);

        SearchRequestBuilder searchRequestBuilderMock = mock(SearchRequestBuilder.class);
        when(searchRequestBuilderMock.execute()).thenReturn(futureMock);
        when(searchRequestBuilderMock.setQuery(org.mockito.Matchers.any(QueryBuilder.class))).then((query) -> {
            // validation logic
           return searchRequestBuilderMock;
        });
        when(searchRequestBuilderMock.setSearchType(SearchType.COUNT)).thenReturn(searchRequestBuilderMock);
        when(searchRequestBuilderMock.setScroll(any(TimeValue.class))).thenReturn(searchRequestBuilderMock);
        when(searchRequestBuilderMock.setSize(anyInt())).thenReturn(searchRequestBuilderMock);
        when(client.prepareSearch()).thenReturn(searchRequestBuilderMock);

        configuration = mock(ElasticGraphConfiguration.class);
    }

    @Test
    public void testSingleIdPromiseVertexWithoutConstraint() throws ExecutionException, InterruptedException {

        UniGraph graph = mock(UniGraph.class);

        //basic edge constraint
        Traversal constraint = __.and(__.has(T.label, "fire"), __.has("direction", "out"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices to start from
        Vertex startVertex1 = mock(Vertex.class);
        when(startVertex1.id()).thenReturn("3");
        when(startVertex1.label()).thenReturn("dragon");

        Vertex startVertex2 = mock(Vertex.class);
        when(startVertex2.id()).thenReturn("13");
        when(startVertex2.label()).thenReturn("dragon");

        SearchVertexQuery searchQuery = mock(SearchVertexQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Edge.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);
        when(searchQuery.getVertices()).thenReturn(Arrays.asList(startVertex1, startVertex2));

        GraphVertexSchema graphVertexSchema = mock(GraphVertexSchema.class);
        when(graphVertexSchema.getType()).thenReturn("type_dragon");

        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);
        when(schemaProvider.getVertexSchema("dragon")).thenReturn(Optional.of(graphVertexSchema));

        SearchPromiseVertexController controller = new SearchPromiseVertexController(client, configuration, graph, schemaProvider);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

    }
}
