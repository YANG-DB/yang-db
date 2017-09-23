package com.kayhut.fuse.unipop.controller.discrete;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.ElementController;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
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
        elasticEmbeddedNode = new ElasticEmbeddedNode("fuse.test_elastic", 3);

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
                                ),
                                new DiscreteVertexController(
                                        elasticEmbeddedNode.getClient(),
                                        elasticGraphConfiguration,
                                        uniGraph,
                                        schemaProvider,
                                        new MetricRegistry())
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
                true,
                "faction",
                false,
                () -> createDragons(10)).populate();

        new ElasticDataPopulator(
                client,
                "coins",
                "Coin",
                "id",
                true,
                "faction",
                true,
                () -> createCoins(10, 3)).populate();

        elasticEmbeddedNode.getClient().admin().indices().refresh(new RefreshRequest("dragons", "coins")).actionGet();
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
        Assert.assertEquals(40, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon") || vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices)
                .filter(vertex -> vertex.label().equals("Dragon"))
                .forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXlabel_DragonX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon").toList();
        Assert.assertEquals(10, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXage_103X_hasXage_select_raw_ageX() throws InterruptedException {
        List<Vertex> vertices = g.V().has("age", P.eq(103)).has("age", SelectP.raw("age")).toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertEquals("Dragon", vertices.get(0).label());
        Assert.assertEquals((Integer)103, vertices.get(0).value("age"));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXfaction_faction1X() {
        List<Vertex> vertices = g.V().has("faction", P.eq("faction1")).toList();
        Assert.assertEquals(2, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction").equals("faction1")));
    }

    @Test
    public void g_V_hasXlabel_DragonX_outE_hasCoin() throws InterruptedException {
        List<Edge> edges = g.V().has(T.label, "Dragon").outE("hasCoin").toList();
        Assert.assertEquals(30, edges.size());
        Assert.assertTrue(Stream.ofAll(edges).forAll(edge -> edge.label().equals("hasCoin")));
    }

    @Test
    public void g_V_hasXlabel_DragonX_out_hasCoin() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon").out("hasCoin").toList();
        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXlabel_DragonX_inE_hasCoin() throws InterruptedException {
        List<Edge> edges = g.V().has(T.label, "Dragon").inE("hasCoin").toList();
        Assert.assertEquals(0, edges.size());
    }

    @Test
    public void g_V_hasXlabel_DragonX_in_hasCoin() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon").in("hasCoin").toList();
        Assert.assertEquals(0, vertices.size());
    }

    @Test
    public void g_V_hasXlabel_DragonX_outE_hasCoin_hasXmaterial_SelectP_raw_materialX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon").outE("hasCoin").has("material", SelectP.raw("material")).inV().toList();
        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material") != null));
    }

    @Test
    public void g_V_hasXlabel_DragonX_outE_hasCoin_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon")
                .outE("hasCoin")
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material") != null));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight") != null));
    }

    @Test
    public void g_V_hasXlabel_DragonX_outE_hasCoin_hasXmaterial_goldX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon")
                .outE("hasCoin")
                .has("material", "gold")
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(8, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material").toString().equals("gold")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight") != null));
    }

    @Test
    public void g_V_hasXlabel_DragonX_outE_hasCoin_hasXweight_30X_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon")
                .outE("hasCoin")
                .has("weight", 30)
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(4, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight").equals(30)));
    }

    @Test
    public void g_V_hasXlabel_DragonX_outE_hasCoin_hasXmaterial_goldX_hasXweight_30X_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.label, "Dragon")
                .outE("hasCoin")
                .has("material", "gold")
                .has("weight", 30)
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material").equals("gold")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight").equals(30)));
    }
    //endregion

    //region SchemaProvider
    private static GraphElementSchemaProvider getSchemaProvider() {
        return new GraphElementSchemaProvider() {
            @Override
            public Optional<GraphVertexSchema> getVertexSchema(String label) {
                switch (label) {
                    case "Dragon": return Optional.of(new GraphVertexSchema() {
                        @Override
                        public String getType() {
                            return "Dragon";
                        }

                        @Override
                        public Optional<GraphElementRouting> getRouting() {
                            return Optional.of(new GraphElementRouting.Impl(
                                    new GraphElementPropertySchema.Impl("faction")
                            ));
                        }

                        @Override
                        public IndexPartitions getIndexPartitions() {
                            return new StaticIndexPartitions(Collections.singletonList("dragons"));
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

                    case "Coin": return Optional.of(new GraphVertexSchema() {
                        @Override
                        public String getType() {
                            return "Coin";
                        }

                        @Override
                        public Optional<GraphElementRouting> getRouting() {
                            return Optional.empty();
                        }

                        @Override
                        public IndexPartitions getIndexPartitions() {
                            return new StaticIndexPartitions(Collections.singletonList("coins"));
                        }

                        @Override
                        public Iterable<GraphElementPropertySchema> getProperties() {
                            return Arrays.asList(
                                    new GraphElementPropertySchema.Impl("material", "string"),
                                    new GraphElementPropertySchema.Impl("weight", "int")
                            );
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
            public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
                switch (label) {
                    case "hasCoin": return Optional.of(new GraphEdgeSchema() {
                        @Override
                        public Optional<End> getSource() {
                            return Optional.of(
                                    new End.Impl("dragonId",
                                            Optional.of("Dragon"),
                                            Collections.emptyList(),
                                            Optional.of(new GraphElementRouting.Impl(
                                                    new GraphElementPropertySchema.Impl("faction")
                                            ))));
                        }

                        @Override
                        public Optional<End> getDestination() {
                            return Optional.of(
                                    new End.Impl("_id",
                                            Optional.of("Coin"),
                                            Arrays.asList(
                                                    new GraphRedundantPropertySchema.Impl("material", "material", "string"),
                                                    new GraphRedundantPropertySchema.Impl("weight", "weight", "int")),
                                            Optional.empty()));
                        }

                        @Override
                        public Optional<Direction> getDirection() {
                            return Optional.empty();
                        }

                        @Override
                        public String getType() {
                            return "Coin";
                        }

                        @Override
                        public String getLabel() {
                            return "hasCoin";
                        }

                        @Override
                        public Optional<GraphElementRouting> getRouting() {
                            return Optional.empty();
                        }

                        @Override
                        public IndexPartitions getIndexPartitions() {
                            return new StaticIndexPartitions(Collections.singletonList("coins"));
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
            public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
                Optional<GraphEdgeSchema> graphEdgeSchema = getEdgeSchema(label);
                return graphEdgeSchema.<Iterable<GraphEdgeSchema>>map(Collections::singletonList).orElseGet(Collections::emptyList);
            }

            @Override
            public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
                return null;
            }

            @Override
            public Iterable<String> getVertexLabels() {
                return Arrays.asList("Dragon", "Coin");
            }

            @Override
            public Iterable<String> getEdgeLabels() {
                return Arrays.asList("hasCoin");
            }
        };
    }
    //endregion

    //region Private Methods
    private static Iterable<Map<String, Object>> createDragons(int numDragons) {
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
        List<String> factions = Arrays.asList("faction1", "faction2", "faction3", "faction4", "faction5");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap();
            dragon.put("id", "d" + Integer.toString(i));
            dragon.put("faction", factions.get(i % factions.size()));
            dragon.put("name", "dragon" + i);
            dragon.put("age", 100 + i);
            dragon.put("color", colors.get(i % colors.size()));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Iterable<Map<String, Object>> createCoins(int numDragons, int numCoinsPerDragon) {
        int coinId = 0;
        List<String> materials = Arrays.asList("gold", "silver", "bronze", "tin");
        List<Integer> weights = Arrays.asList(10, 20, 30, 40, 50, 60, 70);
        List<String> factions = Arrays.asList("faction1", "faction2", "faction3", "faction4", "faction5");

        List<Map<String, Object>> coins = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            for(int j = 0; j < numCoinsPerDragon ; j++) {
                Map<String, Object> coin = new HashMap();
                coin.put("id", "c" + Integer.toString(coinId));
                coin.put("faction", factions.get(i % factions.size()));
                coin.put("dragonId", "d" + Integer.toString(i));
                coin.put("material", materials.get(coinId % materials.size()));
                coin.put("weight", weights.get(coinId % weights.size()));
                coins.add(coin);

                coinId++;
            }
        }

        return coins;
    }
    //endregion

    //region Fields
    private GraphTraversalSource g;
    //endregion
}
