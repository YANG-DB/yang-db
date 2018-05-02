package com.kayhut.fuse.unipop.controller.discrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.ElementController;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.structure.FuseUniGraph;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.GlobalElasticEmbeddedNode;
import com.kayhut.test.framework.index.Mappings;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.*;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.*;

import static com.kayhut.fuse.unipop.controller.promise.GlobalConstants.HasKeys.CONSTRAINT;
import static com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endA;
import static com.kayhut.test.framework.index.Mappings.Mapping.Property.Type.keyword;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteElementReduceControllerTests {
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
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();

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
        graph = new FuseUniGraph(
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
                                                schemaProvider),
                                        null
                                ),
                                new DiscreteVertexController(
                                        elasticEmbeddedNode.getClient(),
                                        elasticGraphConfiguration,
                                        uniGraph,
                                        schemaProvider),
                                new DiscreteElementReduceController(
                                        elasticEmbeddedNode.getClient(),
                                        elasticGraphConfiguration,
                                        uniGraph,
                                        schemaProvider)
                        );
                    }

                    @Override
                    public void close() {

                    }
                },
                new StandardStrategyProvider());

        TransportClient client = elasticEmbeddedNode.getClient();
        client.admin().indices().preparePutTemplate("all")
                .setTemplate("*")
                .setSettings(Settings.builder()
                        .put("number_of_shards", 1)
                        .put("number_of_replicas", 0).build())
                .addMapping("pge", new ObjectMapper().writeValueAsString(new Mappings.Mapping()
                        .addProperty("type", new Mappings.Mapping.Property(keyword))), XContentType.JSON)
                .execute().actionGet();

        new ElasticDataPopulator(client, "dragons1", "pge", "id", true, "faction", false, () -> createDragons(0, 5)).populate();
        new ElasticDataPopulator(client, "dragons2", "pge", "id", true, "faction", false, () -> createDragons(5, 10)).populate();
        new ElasticDataPopulator(client, "coins1", "pge", "id", true, "faction", true, () -> createCoins(0, 5, 3)).populate();
        new ElasticDataPopulator(client, "coins2", "pge", "id", true, "faction", true, () -> createCoins(5, 10, 3)).populate();

        Iterable<Map<String, Object>> fireEventsDual1 = createFireEventsDual(0, 5, 10, 3);
        Iterable<Map<String, Object>> fireEventsDual2 = createFireEventsDual(5, 10, 10, 3);
        new ElasticDataPopulator(client, "dragons1", "pge", "id", true, "entityAId", false,
                () -> Stream.ofAll(fireEventsDual1)
                        .appendAll(fireEventsDual2)
                        .filter(fireEvent -> Integer.parseInt(((String) fireEvent.get("entityAId")).substring(1)) < 5))
                .populate();
        new ElasticDataPopulator(client, "dragons2", "pge", "id", true, "entityAId", false,
                () -> Stream.ofAll(fireEventsDual1)
                        .appendAll(fireEventsDual2)
                        .filter(fireEvent -> Integer.parseInt(((String) fireEvent.get("entityAId")).substring(1)) >= 5))
                .populate();

        new ElasticDataPopulator(client, "fire1", "pge", "id", true, null, false, () -> createFireEventsSingular(0, 5, 10, 3)).populate();
        new ElasticDataPopulator(client, "fire2", "pge", "id", true, null, false, () -> createFireEventsSingular(5, 10, 10, 3)).populate();

        elasticEmbeddedNode.getClient().admin().indices().refresh(
                new RefreshRequest("dragons1", "dragons2", "coins1", "coins2", "fire1", "fire2")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        elasticEmbeddedNode.getClient().admin().indices().prepareDelete("dragons1", "dragons2", "coins1", "coins2", "fire1", "fire2").execute().actionGet();
    }

    @Before
    public void before() {
        g = graph.traversal();
    }
    //endregion

    //region Tests

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin() throws InterruptedException {
        assertCountEquals(30, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).outE("hasCoin"));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX() throws InterruptedException {
        assertCountEquals(30, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_inE_hasCoin() throws InterruptedException {
        GraphTraversal<Vertex, Edge> vertexEdgeGraphTraversal = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).inE("hasCoin");
        assertCountEquals(0, vertexEdgeGraphTraversal);
    }



    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_inE_hasXconstraint_byXhasXlabel_hasCoinXXX() throws InterruptedException {
        assertCountEquals(0, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))));
    }


    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXmaterial_SelectP_raw_materialX_inV() throws InterruptedException {
        assertCountEquals(30, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).outE("hasCoin").has("material", SelectP.raw("material")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_hasXmaterial_SelectP_raw_materialX_inV() throws InterruptedException {
        assertCountEquals(30, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin")))
                .has("material", SelectP.raw("material")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        assertCountEquals(30, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE("hasCoin")
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {

        assertCountEquals(30, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin")))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXconstraint_byXhasXmaterial_goldXXX_hasXmaterial_goldX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        assertCountEquals(8, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE("hasCoin").has(CONSTRAINT, Constraint.by(__.has("material", "gold")))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));


    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXandXhasXlabel_hasCoinX_hasXmaterial_goldXXXX_hasXmaterial_goldX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        assertCountEquals(8, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "hasCoin"), __.has("material", "gold"))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));

    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXconstraint_byXhasXweight_30XXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        assertCountEquals(4, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE("hasCoin").has(CONSTRAINT, Constraint.by(__.has("weight", 30)))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXandXhasXlabel_hasCoinX_hasXweight_30XXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        assertCountEquals(4, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "hasCoin"), __.has("weight", 30))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));

    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXconstraint_byXandXhasXmaterial_goldX_hasXweight_30XXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        assertCountEquals(1, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE("hasCoin").has(CONSTRAINT, Constraint.by(__.and(__.has("material", "gold"), __.has("weight", 30))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXandXhasXlabel_hasCoinX_hasXmaterial_goldX_hasXweight_30XXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        assertCountEquals(1, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.and(
                        __.has(T.label, "hasCoin"),
                        __.has("material", "gold"),
                        __.has("weight", 30))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight")));
    }


    @Test
    public void g_V_hasXconstraint_byXhasXlabel_FireXXX_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        assertCountEquals(30, g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Fire")))
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_FireXXX_outE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        assertCountEquals(0,g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Fire")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))));
    }


    //endregion

    //region SchemaProvider
    private static GraphElementSchemaProvider getSchemaProvider() {
        List<IndexPartitions.Partition> dragonPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("d000", "d005", "dragons1"),
                new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "dragons2"));

        List<IndexPartitions.Partition> coinPartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("d000", "d005", "coins1"),
                new IndexPartitions.Partition.Range.Impl<>("d005", "d010", "coins2"));

        List<IndexPartitions.Partition> firePartitions = Arrays.asList(
                new IndexPartitions.Partition.Range.Impl<>("f0000", "f0015", "fire1"),
                new IndexPartitions.Partition.Range.Impl<>("f0015", "f0030", "fire2"));

        return new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl(
                                "Dragon",
                                new GraphElementConstraint.Impl(__.has(T.label, "Dragon")),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("faction")
                                )),
                                Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)),
                                Collections.emptyList()),
                        new GraphVertexSchema.Impl(
                                "Coin",
                                new GraphElementConstraint.Impl(__.has(T.label, "Coin")),
                                Optional.empty(),
                                Optional.of(new IndexPartitions.Impl("dragonId", coinPartitions)),
                                Arrays.asList(
                                        new GraphElementPropertySchema.Impl("material", "string"),
                                        new GraphElementPropertySchema.Impl("weight", "int"))),
                        new GraphVertexSchema.Impl(
                                "Fire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireSingular")),
                                Optional.empty(),
                                Optional.of(new IndexPartitions.Impl("_id", firePartitions)),
                                Collections.emptyList())),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                "hasCoin",
                                new GraphElementConstraint.Impl(__.has(T.label, "Coin")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("dragonId"),
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("faction")
                                        )),
                                        Optional.of(new IndexPartitions.Impl("_id", coinPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Coin"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("material", "material", "string"),
                                                new GraphRedundantPropertySchema.Impl("weight", "weight", "int")),
                                        Optional.empty(),
                                        Optional.of(new IndexPartitions.Impl("dragonId", coinPartitions)))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasOutFire",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "FireDual"), __.has("direction", Direction.OUT.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("fireId"),
                                        Optional.of("Fire"),
                                        Collections.singletonList(new GraphRedundantPropertySchema.Impl("duration", "duration", "int")))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasOutFire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireSingular")),
                                Optional.of(new GraphEdgeSchema.End.Impl(Collections.singletonList("entityAId"), Optional.of("Dragon"))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Fire"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(new IndexPartitions.Impl("_id", firePartitions)))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInFire",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "FireDual"), __.has("direction", Direction.IN.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("fireId"),
                                        Optional.of("Fire"),
                                        Collections.singletonList(new GraphRedundantPropertySchema.Impl("duration", "duration", "int")))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInFire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireSingular")),
                                Optional.of(new GraphEdgeSchema.End.Impl(Collections.singletonList("entityBId"), Optional.of("Dragon"))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("_id"),
                                        Optional.of("Fire"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(new IndexPartitions.Impl("_id", firePartitions)))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.endB).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasFire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireDual")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("fireId"),
                                        Optional.of("Fire"),
                                        Collections.singletonList(new GraphRedundantPropertySchema.Impl("duration", "duration", "int")))),
                                Direction.OUT,
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "fire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireDual")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(Collections.singletonList("entityBId"), Optional.of("Dragon"), Collections.emptyList())),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "fire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireDual")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityAId"),
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(Collections.singletonList("entityBId"), Optional.of("Dragon"), Collections.emptyList())),
                                Direction.IN,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet())
                        ));
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
            dragon.put("type", "Dragon");
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
                coin.put("type", "Coin");
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

    private static Iterable<Map<String, Object>> createFireEventsDual(int dragonStartId, int dragonEndId, int totalNumDragons, int numFireEventsPerDragon) {
        int fireEventId = dragonStartId * numFireEventsPerDragon;
        int fireDocEventId = fireEventId * 2;

        List<Map<String, Object>> fireEvents = new ArrayList<>();
        for(int i = dragonStartId ; i < dragonEndId ; i++) {
            for(int j = 0 ; j < numFireEventsPerDragon ; j++) {
                Map<String, Object> fireEvent1 = new HashMap<>();
                Map<String, Object> fireEvent2 = new HashMap<>();

                String sourceDragonId = "d" + String.format("%03d", i);
                String destDragonId = "d" + String.format("%03d", (i + j) % totalNumDragons);

                fireEvent1.put("type", "FireDual");
                fireEvent1.put("entityAId", sourceDragonId);
                fireEvent1.put("entityBId", destDragonId);
                fireEvent1.put("direction", Direction.OUT.toString().toLowerCase());
                fireEvent2.put("type", "FireDual");
                fireEvent2.put("entityBId", sourceDragonId);
                fireEvent2.put("entityAId", destDragonId);
                fireEvent2.put("direction", Direction.IN.toString().toLowerCase());

                int duration = dragonStartId * 100 + j;
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

    private static Iterable<Map<String, Object>> createFireEventsSingular(int dragonStartId, int dragonEndId, int totalNumDragons, int numFireEventsPerDragon) {
        int fireEventId = dragonStartId * numFireEventsPerDragon;

        List<Map<String, Object>> fireEvents = new ArrayList<>();
        for(int i = dragonStartId ; i < dragonEndId ; i++) {
            for(int j = 0 ; j < numFireEventsPerDragon ; j++) {
                Map<String, Object> fireEvent = new HashMap<>();

                String sourceDragonId = "d" + String.format("%03d", i);
                String destDragonId = "d" + String.format("%03d", (i + j) % totalNumDragons);

                fireEvent.put("type", "FireSingular");
                fireEvent.put("entityAId", sourceDragonId);
                fireEvent.put("entityBId", destDragonId);

                int duration = i * 100 + j;
                fireEvent.put("duration", duration);

                fireEvent.put("id", "f" + String.format("%04d", fireEventId++));

                fireEvents.add(fireEvent);
            }
        }

        return fireEvents;
    }
    private void assertCountEquals(long expectedCount, GraphTraversal<Vertex, Edge> vertexEdgeGraphTraversal){
        long count = vertexEdgeGraphTraversal.count().next();
        Assert.assertEquals(expectedCount, count);
    }
    //endregion

    //region Fields
    private GraphTraversalSource g;
    //endregion
}
