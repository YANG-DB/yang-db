package com.yangdb.fuse.services.engine2.data;

import com.codahale.metrics.MetricRegistry;
import com.yangdb.fuse.services.TestsConfiguration;
import com.yangdb.fuse.services.engine2.NonRedundantTestSuite;
import com.yangdb.fuse.unipop.controller.ElasticGraphConfiguration;
import com.yangdb.fuse.unipop.controller.common.logging.LoggingSearchVertexController;
import com.yangdb.fuse.unipop.controller.promise.PromiseVertexController;
import com.yangdb.fuse.unipop.controller.promise.PromiseVertexFilterController;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.fuse.test.framework.populator.ElasticDataPopulator;
import com.yangdb.test.BaseITMarker;
import javaslang.collection.Stream;
import org.apache.commons.collections.map.HashedMap;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.joda.time.DateTime;
import org.junit.*;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Elad on 4/25/2017.
 */
public class PromiseEdgeIT implements BaseITMarker {
    static TransportClient client;
    static ElasticGraphConfiguration configuration;
    static UniGraph graph;
    static MetricRegistry registry;

    private static final String INDEX_NAME = "v1";

    @BeforeClass
    public static void setup() throws Exception {
        String idField = "id";
        registry = new MetricRegistry();

        client = NonRedundantTestSuite.elasticEmbeddedNode.getClient();

        new ElasticDataPopulator(
                client,
                INDEX_NAME,
                "pge",
                idField,
                () -> createDragons(10)).populate();

        new ElasticDataPopulator(
                client,
                INDEX_NAME,
                "pge",
                idField,
                () -> createFire(100)).populate();

        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();

        configuration = mock(ElasticGraphConfiguration.class);

        graph = mock(UniGraph.class);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        NonRedundantTestSuite.elasticEmbeddedNode.getClient().admin().indices()
                .delete(new DeleteIndexRequest(INDEX_NAME)).actionGet();
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }


    @Test
    public void testPromiseEdges() {

        //basic edge constraint
        Traversal constraint = __.and(__.has(T.label, "fire"), __.has("direction", "out"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices getTo start getFrom
        Vertex startVertex1 = mock(Vertex.class);
        when(startVertex1.id()).thenReturn("d1");
        when(startVertex1.label()).thenReturn("Dragon");

        Vertex startVertex2 = mock(Vertex.class);
        when(startVertex2.id()).thenReturn("d2");
        when(startVertex2.label()).thenReturn("Dragon");

        Vertex startVertex6 = mock(Vertex.class);
        when(startVertex6.id()).thenReturn("d6");
        when(startVertex6.label()).thenReturn("Dragon");

        Vertex startVertex8 = mock(Vertex.class);
        when(startVertex8.id()).thenReturn("d8");
        when(startVertex8.label()).thenReturn("Dragon");

        //prepare searchVertexQuery for the controller input
        SearchVertexQuery searchQuery = mock(SearchVertexQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Edge.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);
        when(searchQuery.getVertices()).thenReturn(Arrays.asList(startVertex1, startVertex2, startVertex6, startVertex8));

        //prepare schema provider
        IndexPartitions indexPartitions = new StaticIndexPartitions(Collections.singletonList("v1"));
        GraphEdgeSchema edgeSchema = mock(GraphEdgeSchema.class);
        when(edgeSchema.getIndexPartitions()).thenReturn(Optional.of(indexPartitions));
        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);
        when(schemaProvider.getEdgeSchemas(any())).thenReturn(Collections.singletonList(edgeSchema));

        LoggingSearchVertexController controller = new LoggingSearchVertexController(new PromiseVertexController(client, configuration, graph, schemaProvider), registry);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        edges.forEach(Assert::assertNotNull);

    }

    @Test
    public void testPromiseFilterEdge() throws Exception {

        //add old purple dragon
        String purpleDragonId = "d11";
        new ElasticDataPopulator(
                client,
                INDEX_NAME,
                "pge",
                "id",
                () -> {
                    Map<String, Object> dragon = new HashedMap();
                    dragon.put("id", purpleDragonId);
                    dragon.put("type", "Dragon");
                    dragon.put("name", "dragon" + purpleDragonId);
                    dragon.put("age", 100);
                    dragon.put("color", "purple");
                    return Arrays.asList(dragon);
                }).populate();
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();


        //edge constraint - this is the constraint that filters the end vertices of the promise edges
        Traversal constraint = __.and(__.has("color", "purple"), __.has("age", P.gt(10)));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);

        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices getTo start getFrom (all)
        List<Vertex> startVertices = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            Vertex v = mock(Vertex.class);
            when(v.id()).thenReturn("d" + i);
            when(v.label()).thenReturn("Dragon");
            startVertices.add(v);
        }

        //prepare searchVertexQuery for the controller input
        SearchVertexQuery searchQuery = mock(SearchVertexQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Edge.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);
        when(searchQuery.getVertices()).thenReturn(startVertices);
        when(searchQuery.getLimit()).thenReturn(-1);

        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);

        when(configuration.getElasticGraphScrollSize()).thenReturn(100);
        when(configuration.getElasticGraphScrollTime()).thenReturn(100);
        when(configuration.getElasticGraphDefaultSearchSize()).thenReturn(100L);

        SearchVertexQuery.SearchVertexController controller = new LoggingSearchVertexController(new PromiseVertexFilterController(client, configuration, graph, schemaProvider,
                context -> SearchOrderProvider.of(SearchOrderProvider.EMPTY, SearchType.DEFAULT)), registry);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        //TODO: Check why it fails when executed immediately after the previous test
        //Assert.assertEquals(1, edges.size());

        edges.forEach(e -> {
            //Verify that the edge's endpoint is the correct vertex
            edges.forEach(Assert::assertNotNull);
            Assert.assertEquals("d11", e.inVertex().id());
        });

    }

    private static Iterable<Map<String, Object>> createDragons(int numDragons) {
        Random r = new Random();
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for (int i = 0; i < numDragons; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", "d" + Integer.toString(i));
            dragon.put("type", "Dragon");
            dragon.put("name", "dragon" + i);
            dragon.put("age", r.nextInt(100));
            dragon.put("color", colors.get(r.nextInt(colors.size())));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Iterable<Map<String, Object>> createFire(int numRels) {
        Random r = new Random();
        List<Map<String, Object>> ownDocs = new ArrayList<>();

        for (int i = 0; i < numRels; i++) {

            Map<String, Object> fire = new HashedMap();

            fire.put("id", "f" + i);
            fire.put("type", "fire");

            Map<String, Object> entityA = new HashMap<>();
            entityA.put("id", "d" + r.nextInt(9));
            fire.put("entityA", entityA);

            Map<String, Object> entityB = new HashMap<>();
            entityB.put("id", "d" + r.nextInt(9));
            fire.put("entityB", entityB);

            fire.put("direction", "out");
            fire.put("time", DateTime.now().toString());

            ownDocs.add(fire);
        }
        return ownDocs;
    }

}
