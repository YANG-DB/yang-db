package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexController;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
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
import org.elasticsearch.client.ElasticsearchClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Elad on 4/25/2017.
 */
public class PromiseEdgeTest{


    private Client client;
    private ElasticGraphConfiguration configuration;

    @Before
    public void setup() throws Exception {
        String indexName = "v1";
        String idField = "id";

        ElasticInMemoryIndex elasticInMemoryIndex = new ElasticInMemoryIndex();

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                indexName,
                "Dragon",
                idField,
                () -> createDragons(10)).populate();

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                indexName,
                "Fire",
                idField,
                () -> createOwns(100)).populate();

        Thread.sleep(2000);

        client = elasticInMemoryIndex.getClient();
    }

    @Test
    public void testPromiseEdges() {

        UniGraph graph = mock(UniGraph.class);

        //basic edge constraint
        Traversal constraint = __.and(__.has(T.label, "Fire"), __.has("direction", "out"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices to start from
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

        SearchVertexQuery searchQuery = mock(SearchVertexQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Edge.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);
        when(searchQuery.getVertices()).thenReturn(Arrays.asList(startVertex1, startVertex2, startVertex6, startVertex8));

        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);
        /*GraphVertexSchema graphVertexSchema = mock(GraphVertexSchema.class);
        when(graphVertexSchema.getType()).thenReturn("type_dragon");
        when(schemaProvider.getVertexSchema("dragon")).thenReturn(Optional.of(graphVertexSchema));*/

        SearchPromiseVertexController controller = new SearchPromiseVertexController(client, configuration, graph, schemaProvider);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        edges.forEach(e -> System.out.println("Promise edge: " + e));

    }

    private Iterable<Map<String, Object>> createDragons(int numDragons) {
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", "d" + Integer.toString(i));
            dragon.put("name", "dragon" + i);
            dragons.add(dragon);
        }
        return dragons;
    }

    private Iterable<Map<String, Object>> createOwns(int numRels) {
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

    private Query buildQuery() {

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);

        //person to start with
        EConcrete source = new EConcrete();
        source.seteNum(1);
        source.seteTag("A");
        source.seteID("p3");
        source.seteType(1);
        source.setNext(2);

        //ownerships relations
        Rel rel = new Rel();
        rel.setrType(1);
        rel.setDir("R");
        rel.seteNum(2);
        rel.setNext(3);

        //dragons
        ETyped dest = new ETyped();
        dest.seteType(2);
        dest.seteTag("B");
        dest.seteNum(3);

        return Query.QueryBuilder.aQuery()
                .withName("promise_edge_test")
                .withOnt("dragons")
                .withElements(Arrays.asList(source, rel, dest))
                .build();
    }

}
