package com.kayhut.fuse.unipop;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexController;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
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
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 23/04/2017.
 */
public class PromisePromiseElementVertexControllerTest {
    Client client;
    ElasticGraphConfiguration configuration;

    @Before
    public void setUp() throws Exception {

        client = mock(Client.class);

        //mock response with 2 layers of aggregations
        SearchResponse responseMock = mock(SearchResponse.class);

        Terms.Bucket destBucket = mock(Terms.Bucket.class);
        when(destBucket.getKeyAsString()).thenReturn("destination1");
        when(destBucket.getDocCount()).thenReturn(1000L);

        InternalTerms destLayer = mock(InternalTerms.class);
        when(destLayer.getName()).thenReturn(GlobalConstants.EdgeSchema.DEST);
        when(destLayer.getBuckets()).then((Answer<Object>)invocationOnMock -> Collections.singletonList(destBucket));

        Terms.Bucket sourceBucket = mock(Terms.Bucket.class);
        when(sourceBucket.getKeyAsString()).thenReturn("source1");
        when(sourceBucket.getDocCount()).thenReturn(1L);
        when(sourceBucket.getAggregations()).thenReturn(new InternalAggregations(Collections.singletonList(destLayer)));

        InternalTerms sourceLayer = mock(InternalTerms.class);
        when(sourceLayer.getName()).thenReturn(GlobalConstants.EdgeSchema.SOURCE);
        when(sourceLayer.getBuckets()).then((Answer<Object>) invocationOnMock -> Collections.singletonList(sourceBucket));

        Aggregations aggregations = new InternalAggregations(Arrays.asList(sourceLayer, destLayer));

        when(responseMock.getAggregations()).thenReturn(aggregations);

        ListenableActionFuture<SearchResponse> futureMock = mock(ListenableActionFuture.class);
        when(futureMock.actionGet()).thenReturn(responseMock);

        SearchRequestBuilder searchRequestBuilderMock = mock(SearchRequestBuilder.class);
        when(searchRequestBuilderMock.execute()).thenReturn(futureMock);
        when(searchRequestBuilderMock.setQuery(org.mockito.Matchers.any(QueryBuilder.class))).then((query) -> {
            // validation logic
           return searchRequestBuilderMock;
        });

        when(searchRequestBuilderMock.setScroll(any(TimeValue.class))).thenReturn(searchRequestBuilderMock);
        when(searchRequestBuilderMock.setSize(anyInt())).thenReturn(searchRequestBuilderMock);
        when(searchRequestBuilderMock.execute()).thenReturn(futureMock);
        when(client.prepareSearch()).thenReturn(searchRequestBuilderMock);

        configuration = mock(ElasticGraphConfiguration.class);
    }

    @Test
    public void testSingleIdPromiseVertexWithoutConstraint() throws ExecutionException, InterruptedException {
        MetricRegistry registry = new MetricRegistry();

        UniGraph graph = mock(UniGraph.class);

        //basic edge constraint
        Traversal constraint = __.and(__.has(T.label, "fire"), __.has("direction", "out"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(
                new HasContainer(T.label.getAccessor(), P.within(GlobalConstants.Labels.PROMISE)),
                new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices getTo start getFrom
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

        //prepare schema provider
        GraphEdgeSchema edgeSchema = mock(GraphEdgeSchema.class);
        when(edgeSchema.getIndexPartitions()).thenReturn(Optional.of(new StaticIndexPartitions(Collections.singletonList("v1"))));
        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);
        when(schemaProvider.getEdgeLabels()).thenReturn(Collections.singletonList("label"));
        when(schemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(edgeSchema));
        when(schemaProvider.getEdgeSchemas(any())).thenReturn(Collections.singletonList(edgeSchema));

        PromiseVertexController controller = new PromiseVertexController(client, configuration, graph, schemaProvider);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        edges.forEach(edge -> System.out.println(edge));

        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(1000L, edges.get(0).property(GlobalConstants.HasKeys.COUNT).value());

    }
}
