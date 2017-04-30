package com.kayhut.fuse.services.engine2.data;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexController;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
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
import org.elasticsearch.client.ElasticsearchClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
public class PromiseEdgeTest{


    private Client client;
    ElasticInMemoryIndex elasticInMemoryIndex;

    @Before
    public void setup() throws Exception {

        String indexName = "v1";
        String idField = "id";

        elasticInMemoryIndex = new ElasticInMemoryIndex();

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

        //prepare searchVertexQuery for the controller input
        SearchVertexQuery searchQuery = mock(SearchVertexQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Edge.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);
        when(searchQuery.getVertices()).thenReturn(Arrays.asList(startVertex1, startVertex2, startVertex6, startVertex8));

        //prepare schema provider
        IndexPartition indexPartition = mock(IndexPartition.class);
        when(indexPartition.getIndices()).thenReturn(Arrays.asList("v1"));
        GraphEdgeSchema edgeSchema = mock(GraphEdgeSchema.class);
        when(edgeSchema.getIndexPartitions()).thenReturn(Arrays.asList(indexPartition));
        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);
        when(schemaProvider.getEdgeSchema(any(),any(),any())).thenReturn(Optional.of(edgeSchema));

        ElasticGraphConfiguration configuration = mock(ElasticGraphConfiguration.class);

        SearchPromiseVertexController controller = new SearchPromiseVertexController(client, configuration, graph, schemaProvider);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        edges.forEach(e -> System.out.println("Promise edge: " + e));

    }

    @After
    public void cleanup() throws Exception {

        elasticInMemoryIndex.close();
        Thread.sleep(2000);
        client.close();
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

}
