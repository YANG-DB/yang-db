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
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
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
        new ElasticDataPopulator(client, "dragons1", "Dragon", "id", true, "faction", false, () -> createDragons(0, 5)).populate();
        new ElasticDataPopulator(client, "dragons2", "Dragon", "id", true, "faction", false, () -> createDragons(5, 10)).populate();
        new ElasticDataPopulator(client, "coins1", "Coin", "id", true, "faction", true, () -> createCoins(0, 5, 3)).populate();
        new ElasticDataPopulator(client, "coins2", "Coin", "id", true, "faction", true, () -> createCoins(5, 10, 3)).populate();

        Iterable<Map<String, Object>> fireEvents1 = createFireEvents(0, 5, 10, 3);
        Iterable<Map<String, Object>> fireEvents2 = createFireEvents(5, 10, 10, 3);

        new ElasticDataPopulator(client, "dragons1", "Fire", "id", true, "entityAId", false,
                () -> Stream.ofAll(fireEvents1)
                        .appendAll(fireEvents2)
                        .filter(fireEvent -> Integer.parseInt(((String)fireEvent.get("entityAId")).substring(1)) < 5))
                .populate();

        new ElasticDataPopulator(client, "dragons2", "Fire", "id", true, "entityAId", false,
                () -> Stream.ofAll(fireEvents1)
                        .appendAll(fireEvents2)
                        .filter(fireEvent -> Integer.parseInt(((String)fireEvent.get("entityAId")).substring(1)) >= 5))
                .populate();

        elasticEmbeddedNode.getClient().admin().indices().refresh(new RefreshRequest("dragons1", "dragons2", "coins1", "coins2")).actionGet();
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
        Assert.assertEquals(70, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon") ||
                vertex.label().equals("Coin") ||
                vertex.label().equals("Fire")));
        Assert.assertTrue(Stream.ofAll(vertices)
                .filter(vertex -> vertex.label().equals("Dragon"))
                .forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXid_d001X() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d001").toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXid_d001X_hasXlabel_DragonX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d001").has(T.label, "Dragon").toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXid_d007X() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d007").toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXid_d007X_hasXlabel_DragonX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d007").has(T.label, "Dragon").toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXid_within_d001_d002X() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, P.within("d001", "d002")).toList();
        Assert.assertEquals(2, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXid_within_d001_d007X() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, P.within("d001", "d007")).toList();
        Assert.assertEquals(2, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
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

    @Test
    public void g_V_hasXid_d001X_outE_hasCoin_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d001").outE("hasCoin").inV().toList();
        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXid_d001X_hasXlabel_DragonX_outE_hasCoin_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d001").has(T.label, "Dragon").outE("hasCoin").inV().toList();
        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXid_d007X_outE_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d007").outE("hasCoin").inV().toList();
        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXid_d007X_hasXlabel_DragonX_outE_hasCoin_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, "d007").has(T.label, "Dragon").outE("hasCoin").inV().toList();
        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXid_within_d001_d002X_outE_hasCoin_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, P.within("d001", "d002")).outE("hasCoin").inV().toList();
        Assert.assertEquals(6, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXid_within_d001_d007X_outE_hasCoin_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(T.id, P.within("d001", "d007")).outE("hasCoin").inV().toList();
        Assert.assertEquals(6, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }
    //endregion

    //region SchemaProvider
    private static GraphElementSchemaProvider getSchemaProvider() {
        return new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl(
                                "Dragon",
                                new GraphElementConstraint.Impl(__.has(T.label, "Dragon")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("faction")
                                )),
                                Optional.of(new IndexPartitions.Impl("_id",
                                        new IndexPartitions.Partition.Range.Impl<>("d001", "d005", "dragons1"),
                                        new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "dragons2"))),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Coin",
                                new GraphElementConstraint.Impl(__.has(T.label, "Coin")),
                                Optional.empty(),
                                Optional.of(new IndexPartitions.Impl("dragonId",
                                        new IndexPartitions.Partition.Range.Impl<>("d001", "d005", "coins1"),
                                        new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "coins2"))),
                                Arrays.asList(
                                        new GraphElementPropertySchema.Impl("material", "string"),
                                        new GraphElementPropertySchema.Impl("weight", "int"))),
                        new GraphVertexSchema.Impl(
                                "Fire",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "Fire"), __.has("direction", "out"))),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("entityAId", "string"))),
                                Optional.of(new IndexPartitions.Impl("entityAId",
                                        new IndexPartitions.Partition.Range.Impl<>("d001", "d005", "dragons1"),
                                        new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "dragons2"))),
                                Collections.emptyList())),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                "hasCoin",
                                new GraphElementConstraint.Impl(__.has(T.label, "Coin")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "dragonId",
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("faction")
                                        )),
                                        Optional.of(new IndexPartitions.Impl("_id",
                                                new IndexPartitions.Partition.Range.Impl<>("d001", "d005", "coins1"),
                                                new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "coins2"))))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Coin"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("material", "material", "string"),
                                                new GraphRedundantPropertySchema.Impl("weight", "weight", "int")),
                                        Optional.empty(),
                                        Optional.of(new IndexPartitions.Impl("dragonId",
                                                new IndexPartitions.Partition.Range.Impl<>("d001", "d005", "coins1"),
                                                new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "coins2"))))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasFire",
                                new GraphElementConstraint.Impl(__.has(T.label, "Fire")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityAId",
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id",
                                                new IndexPartitions.Partition.Range.Impl<>("d001", "d005", "dragons1"),
                                                new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "dragons2"))))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "fireId",
                                        Optional.of("Fire"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("entityAId", "string"))),
                                        Optional.of(new IndexPartitions.Impl("entityAId",
                                                new IndexPartitions.Partition.Range.Impl<>("d001", "d005", "dragons1"),
                                                new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "dragons2"))))),
                                Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList())));
    }
    //endregion

    //region Private Methods
    private static Iterable<Map<String, Object>> createDragons(int startId, int endId) {
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
        List<String> factions = Arrays.asList("faction1", "faction2", "faction3", "faction4", "faction5");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = startId ; i < endId ; i++) {
            Map<String, Object> dragon = new HashMap();
            dragon.put("id", "d" + String.format("%03d", i));
            dragon.put("faction", factions.get(i % factions.size()));
            dragon.put("name", "dragon" + i);
            dragon.put("age", 100 + i);
            dragon.put("color", colors.get(i % colors.size()));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Iterable<Map<String, Object>> createCoins(int dragonStartId, int dragonEndId, int numCoinsPerDragon) {
        int coinId = dragonStartId * numCoinsPerDragon;
        List<String> materials = Arrays.asList("gold", "silver", "bronze", "tin");
        List<Integer> weights = Arrays.asList(10, 20, 30, 40, 50, 60, 70);
        List<String> factions = Arrays.asList("faction1", "faction2", "faction3", "faction4", "faction5");

        List<Map<String, Object>> coins = new ArrayList<>();
        for(int i = dragonStartId ; i < dragonEndId ; i++) {
            for(int j = 0; j < numCoinsPerDragon ; j++) {
                Map<String, Object> coin = new HashMap();
                coin.put("id", "c" + Integer.toString(coinId));
                coin.put("faction", factions.get(i % factions.size()));
                coin.put("dragonId", "d" + String.format("%03d", i));
                coin.put("material", materials.get(coinId % materials.size()));
                coin.put("weight", weights.get(coinId % weights.size()));
                coins.add(coin);

                coinId++;
            }
        }

        return coins;
    }

    private static Iterable<Map<String, Object>> createFireEvents(int dragonStartId, int dragonEndId, int totalNumDragons, int numFireEventsPerDragon) {
        int fireEventId = dragonStartId * numFireEventsPerDragon * 2;
        int fireDocEventId = fireEventId;

        List<Map<String, Object>> fireEvents = new ArrayList<>();
        for(int i = dragonStartId ; i < dragonEndId ; i++) {
            for(int j = 0 ; j < numFireEventsPerDragon ; j++) {
                Map<String, Object> fireEvent1 = new HashMap<>();
                Map<String, Object> fireEvent2 = new HashMap<>();

                String sourceDragonId = "d" + String.format("%03d", i);
                String destDragonId = "d" + String.format("%03d", (i + j) % totalNumDragons);

                fireEvent1.put("entityAId", sourceDragonId);
                fireEvent1.put("entityBId", destDragonId);
                fireEvent1.put("direction", "out");
                fireEvent2.put("entityBId", sourceDragonId);
                fireEvent2.put("entityAId", destDragonId);
                fireEvent2.put("direction", "in");

                int duration = dragonStartId * 100 + 1;
                fireEvent1.put("duration", duration);
                fireEvent2.put("duration", duration);

                String logicalEventId = "f" + String.format("%04d", fireEventId++);
                fireEvent1.put("fireId", logicalEventId);
                fireEvent2.put("fireId", logicalEventId);

                fireEvent1.put("id", "f" + fireDocEventId++);
                fireEvent2.put("id", "f" + fireDocEventId++);

                fireEvents.addAll(Arrays.asList(fireEvent1, fireEvent2));
            }
        }

        return fireEvents;
    }
    //endregion

    //region Fields
    private GraphTraversalSource g;
    //endregion
}
