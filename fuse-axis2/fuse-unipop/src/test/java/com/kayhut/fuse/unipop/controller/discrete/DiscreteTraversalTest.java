package com.kayhut.fuse.unipop.controller.discrete;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.ElementController;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteTraversalTest {
    //region Static Fields
    public static ElasticEmbeddedNode elasticEmbeddedNode;
    public static ElasticGraphConfiguration elasticGraphConfiguration;
    public static UniGraphConfiguration uniGraphConfiguration;
    public static UniGraph graph;
    public static GraphElementSchemaProvider schemaProvider;
    //endregion

    //region Setup
    @BeforeClass
    public static void setup() throws Exception {
        elasticEmbeddedNode = new ElasticEmbeddedNode("fuse.test_elastic");

        elasticGraphConfiguration = new ElasticGraphConfiguration();
        elasticGraphConfiguration.setClusterName("fuse.test_elastic");
        elasticGraphConfiguration.setElasticGraphScrollSize(1000);
        elasticGraphConfiguration.setElasticGraphMaxSearchSize(1000);
        elasticGraphConfiguration.setElasticGraphDefaultSearchSize(1000);
        elasticGraphConfiguration.setElasticGraphScrollTime(600000);

        schemaProvider = getSchemaProvider();

        uniGraphConfiguration = new UniGraphConfiguration();
        uniGraphConfiguration.setBulkMax(1000);
        uniGraphConfiguration.setBulkStart(1000);
        graph = new UniGraph(
                uniGraphConfiguration,
                uniGraph -> new ControllerManager() {
                    @Override
                    public Set<UniQueryController> getControllers() {
                        return ImmutableSet.of(
                                new ElementController(
                                        new DiscreteElementVertexController(
                                                elasticEmbeddedNode.getClient(),
                                                elasticGraphConfiguration,
                                                uniGraph,
                                                schemaProvider,
                                                new MetricRegistry()),
                                        null,
                                        new MetricRegistry()
                                )
                        );
                    }

                    @Override
                    public void close() {

                    }
                },
                new StandardStrategyProvider());

        TransportClient client = elasticEmbeddedNode.getClient();
        new ElasticDataPopulator(
                client,
                "dragons",
                "Dragon",
                "id",
                () -> createDragons(10)).populate();

        elasticEmbeddedNode.getClient().admin().indices().refresh(new RefreshRequest("dragons")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (elasticEmbeddedNode != null) {
            if (elasticEmbeddedNode.getClient() != null) {
                elasticEmbeddedNode.getClient().close();
            }

            elasticEmbeddedNode.close();
        }
    }

    @Before
    public void before() {
        g = graph.traversal();
    }
    //endregion

    //region Tests
    @Test
    public void g_V() throws InterruptedException {
        List<Vertex> vertices = g.V().toList();
        Assert.assertEquals(10, vertices.size());
        for(Vertex vertex : vertices) {
            Assert.assertEquals("Dragon", vertex.label());
        }
    }

    @Test
    public void g_V_hasXage_eq_103XXselect_ageX() throws InterruptedException {
        List<Vertex> vertices = g.V().has("age", P.eq(103)).has("age", SelectP.raw("age")).toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertEquals("Dragon", vertices.get(0).label());
        Assert.assertEquals((Integer)103, vertices.get(0).value("age"));
    }
    //endregion

    //region SchemaProvider
    private static GraphElementSchemaProvider getSchemaProvider() {
        return new GraphElementSchemaProvider() {
            @Override
            public Optional<GraphVertexSchema> getVertexSchema(String type) {
                switch (type) {
                    case "Dragon": return Optional.of(new GraphVertexSchema() {
                        @Override
                        public String getType() {
                            return "Dragon";
                        }

                        @Override
                        public Optional<GraphElementRouting> getRouting() {
                            return null;
                        }

                        @Override
                        public IndexPartition getIndexPartition() {
                            return () -> Arrays.asList("dragons");
                        }

                        @Override
                        public Iterable<GraphElementPropertySchema> getProperties() {
                            return null;
                        }

                        @Override
                        public Optional<GraphElementPropertySchema> getProperty(String name) {
                            return null;
                        }
                    });
                }

                return Optional.empty();
            }

            @Override
            public Optional<GraphEdgeSchema> getEdgeSchema(String type) {
                return null;
            }

            @Override
            public Iterable<GraphEdgeSchema> getEdgeSchemas(String type) {
                return null;
            }

            @Override
            public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
                return null;
            }

            @Override
            public Iterable<String> getVertexTypes() {
                return Arrays.asList("Dragon");
            }

            @Override
            public Iterable<String> getEdgeTypes() {
                return null;
            }
        };
    }
    //endregion

    //region Private Methods
    private static Iterable<Map<String, Object>> createDragons(int numDragons) {
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap();
            dragon.put("id", "d" + Integer.toString(i));
            dragon.put("name", "dragon" + i);
            dragon.put("age", 100 + i);
            dragon.put("color", colors.get(i % colors.size()));
            dragons.add(dragon);
        }
        return dragons;
    }
    //endregion

    //region Fields
    private GraphTraversalSource g;
    //endregion
}
