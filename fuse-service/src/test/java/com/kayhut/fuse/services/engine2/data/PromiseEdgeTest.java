package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexController;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexFilterController;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.test.framework.index.ElasticInMemoryIndex;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.commons.collections.map.HashedMap;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.client.Client;
import org.junit.*;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.io.IOException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Elad on 4/25/2017.
 */
public class PromiseEdgeTest{


    private static Client client;
    static ElasticInMemoryIndex elasticInMemoryIndex;
    static ElasticGraphConfiguration configuration;
    static UniGraph graph;

    @BeforeClass
    public static void setup() throws Exception {

        String indexName = "v1";
        String idField = "id";

        elasticInMemoryIndex = new ElasticInMemoryIndex();

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                indexName,
                "dragon",
                idField,
                () -> createDragons(10)).populate();

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                indexName,
                "Fire",
                idField,
                () -> createFire(100)).populate();

        Thread.sleep(2000);

        client = elasticInMemoryIndex.getClient();

        configuration = mock(ElasticGraphConfiguration.class);

        graph = mock(UniGraph.class);
    }

    @Test
    public void testPromiseEdges() {

        //basic relation constraint
        Traversal constraint = __.and(__.has(T.label, "Fire"), __.has("direction", "out"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices to start from
        Vertex startVertex1 = mock(Vertex.class);
        when(startVertex1.id()).thenReturn("d1");
        when(startVertex1.label()).thenReturn("dragon");

        Vertex startVertex2 = mock(Vertex.class);
        when(startVertex2.id()).thenReturn("d2");
        when(startVertex2.label()).thenReturn("dragon");

        Vertex startVertex6 = mock(Vertex.class);
        when(startVertex6.id()).thenReturn("d6");
        when(startVertex6.label()).thenReturn("dragon");

        Vertex startVertex8 = mock(Vertex.class);
        when(startVertex8.id()).thenReturn("d8");
        when(startVertex8.label()).thenReturn("dragon");

        //prepare searchVertexQuery for the controller input
        SearchVertexQuery searchQuery = mock(SearchVertexQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Edge.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);
        when(searchQuery.getVertices()).thenReturn(Arrays.asList(startVertex1, startVertex2, startVertex6, startVertex8));

        //prepare schema provider
        IndexPartition indexPartition = mock(IndexPartition.class);
        when(indexPartition.getIndices()).thenReturn(Arrays.asList("v1"));
        GraphEdgeSchema edgeSchema = mock(GraphEdgeSchema.class);
        when(edgeSchema.getIndexPartition()).thenReturn(indexPartition);
        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);
        when(schemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(edgeSchema));

        SearchPromiseVertexController controller = new SearchPromiseVertexController(client, configuration, graph, schemaProvider);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        edges.forEach(e -> System.out.println("Promise relation: " + e));

    }

    @Test
    public void testPromiseFilterEdge() throws Exception {

        //add old purple dragon
        String purpleDragonId = "d11";
        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                "v1",
                "dragon",
                "id",
                () -> {
                    Map<String, Object> dragon = new HashedMap();
                    dragon.put("id", purpleDragonId);
                    dragon.put("name", "dragon" + purpleDragonId);
                    dragon.put("age", 100);
                    dragon.put("color", "purple");
                    return Arrays.asList(dragon);
                }).populate();


        //relation constraint - this is the constraint that filters the end vertices of the promise edges
        Traversal constraint = __.and(__.has("color", "purple"), __.has("age", P.gt(10)));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);

        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices to start from (all)
        List<Vertex> startVertices = new ArrayList<>();
        for(int i=0; i<13; i++) {
            Vertex v = mock(Vertex.class);
            when(v.id()).thenReturn("d" + i);
            when(v.label()).thenReturn("dragon");
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

        SearchPromiseVertexFilterController controller = new SearchPromiseVertexFilterController(client, configuration, graph, schemaProvider);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        //TODO: Check why it fails when executed immediately after the previous test
        //Assert.assertEquals(1, edges.size());

        edges.forEach(e -> {
            //Verify that the relation's endpoint is the correct vertex
            System.out.println("Promise Filter relation: " + e);
            Assert.assertEquals("d11", e.inVertex().id());
        });

    }

    @AfterClass
    public static void cleanup() throws Exception {

        elasticInMemoryIndex.close();
        Thread.sleep(2000);
        client.close();
    }

    private static Iterable<Map<String, Object>> createDragons(int numDragons) {
        Random r = new Random();
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", "d" + Integer.toString(i));
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

        for(int i = 0 ; i < numRels ; i++) {

            Map<String, Object> own = new HashedMap();

            own.put("id", "f" + i);

            Map<String, Object> entityA = new HashMap<>();
            entityA.put("id", "d" + r.nextInt(9));
            own.put("entityA", entityA);

            Map<String, Object> entityB = new HashMap<>();
            entityB.put("id", "d" + r.nextInt(9));
            own.put("entityB", entityB);

            own.put("direction", "out");

            ownDocs.add(own);
        }
        return ownDocs;
    }

}
