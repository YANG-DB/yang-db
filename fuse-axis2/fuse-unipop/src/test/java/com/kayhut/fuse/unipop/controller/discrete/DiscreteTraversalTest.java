package com.kayhut.fuse.unipop.controller.discrete;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.ElementController;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static com.kayhut.fuse.unipop.controller.promise.GlobalConstants.HasKeys.CONSTRAINT;

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

        Iterable<Map<String, Object>> fireEventsDual1 = createFireEventsDual(0, 5, 10, 3);
        Iterable<Map<String, Object>> fireEventsDual2 = createFireEventsDual(5, 10, 10, 3);
        new ElasticDataPopulator(client, "dragons1", "FireDual", "id", true, "entityAId", false,
                () -> Stream.ofAll(fireEventsDual1)
                        .appendAll(fireEventsDual2)
                        .filter(fireEvent -> Integer.parseInt(((String) fireEvent.get("entityAId")).substring(1)) < 5))
                .populate();
        new ElasticDataPopulator(client, "dragons2", "FireDual", "id", true, "entityAId", false,
                () -> Stream.ofAll(fireEventsDual1)
                        .appendAll(fireEventsDual2)
                        .filter(fireEvent -> Integer.parseInt(((String) fireEvent.get("entityAId")).substring(1)) >= 5))
                .populate();

        new ElasticDataPopulator(client, "fire1", "FireSingular", "id", true, null, false, () -> createFireEventsSingular(0, 5, 10, 3)).populate();
        new ElasticDataPopulator(client, "fire2", "FireSingular", "id", true, null, false, () -> createFireEventsSingular(5, 10, 10, 3)).populate();

        elasticEmbeddedNode.getClient().admin().indices().refresh(
                new RefreshRequest("dragons1", "dragons2", "coins1", "coins2", "fire1", "fire2")).actionGet();
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
    public void g_V_hasXconstraint_byXhasXid_d001XXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, "d001"))).toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertEquals("d001", vertices.get(0).id());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_d001X_hasXlabel_DragonXXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, "d001").has(T.label, "Dragon"))).toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertEquals("d001", vertices.get(0).id());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_d007XXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, "d007"))).toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertEquals("d007", vertices.get(0).id());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_d007X_hasXlabel_DragonXXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, "d007").has(T.label, "Dragon"))).toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertEquals("d007", vertices.get(0).id());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_within_d001_d002XXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, P.within("d001", "d002")))).toList();
        Assert.assertEquals(2, vertices.size());
        Assert.assertEquals(Arrays.asList("d001", "d002"),
                Stream.ofAll(vertices).map(vertex -> vertex.id()).sorted().toJavaList());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_within_d001_d007XXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, P.within("d001", "d007")))).toList();
        Assert.assertEquals(2, vertices.size());
        Assert.assertEquals(Arrays.asList("d001", "d007"),
                Stream.ofAll(vertices).map(vertex -> vertex.id()).sorted().toJavaList());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).toList();
        Assert.assertEquals(10, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXage_103XXX_hasXage_select_raw_ageX() throws InterruptedException {
        List<Vertex> vertices = g.V()
                .has(CONSTRAINT, Constraint.by(__.has("age", P.eq(103))))
                .has("age", SelectP.raw("age")).toList();

        Assert.assertEquals(1, vertices.size());
        Assert.assertEquals("Dragon", vertices.get(0).label());
        Assert.assertEquals((Integer)103, vertices.get(0).value("age"));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXfaction_faction1XXX() {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has("faction", P.eq("faction1")))).toList();
        Assert.assertEquals(2, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("faction").equals("faction1")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin() throws InterruptedException {
        List<Edge> edges = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).outE("hasCoin").toList();
        Assert.assertEquals(30, edges.size());
        Assert.assertTrue(Stream.ofAll(edges).forAll(edge -> edge.label().equals("hasCoin")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX() throws InterruptedException {
        List<Edge> edges = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).toList();
        Assert.assertEquals(30, edges.size());
        Assert.assertTrue(Stream.ofAll(edges).forAll(edge -> edge.label().equals("hasCoin")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_out_hasCoin() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).out("hasCoin").toList();
        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_inE_hasCoin() throws InterruptedException {
        List<Edge> edges = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).inE("hasCoin").toList();
        Assert.assertEquals(0, edges.size());
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_inE_hasXconstraint_byXhasXlabel_hasCoinXXX() throws InterruptedException {
        List<Edge> edges = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).toList();
        Assert.assertEquals(0, edges.size());
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_in_hasCoin() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).in("hasCoin").toList();
        Assert.assertEquals(0, vertices.size());
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXmaterial_SelectP_raw_materialX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon"))).outE("hasCoin").has("material", SelectP.raw("material")).inV().toList();
        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_hasXmaterial_SelectP_raw_materialX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin")))
                .has("material", SelectP.raw("material")).inV().toList();

        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
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
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin")))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material") != null));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXconstraint_byXhasXmaterial_goldXXX_hasXmaterial_goldX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE("hasCoin").has(CONSTRAINT, Constraint.by(__.has("material", "gold")))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(8, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material").toString().equals("gold")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXandXhasXlabel_hasCoinX_hasXmaterial_goldXXXX_hasXmaterial_goldX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "hasCoin"), __.has("material", "gold"))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(8, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material").toString().equals("gold")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXconstraint_byXhasXweight_30XXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE("hasCoin").has(CONSTRAINT, Constraint.by(__.has("weight", 30)))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(4, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight").equals(30)));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXandXhasXlabel_hasCoinX_hasXweight_30XXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "hasCoin"), __.has("weight", 30))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(4, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight").equals(30)));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasCoin_hasXconstraint_byXandXhasXmaterial_goldX_hasXweight_30XXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE("hasCoin").has(CONSTRAINT, Constraint.by(__.and(__.has("material", "gold"), __.has("weight", 30))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material").equals("gold")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight").equals(30)));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXandXhasXlabel_hasCoinX_hasXmaterial_goldX_hasXweight_30XXXX_hasXmaterial_SelectP_raw_materialX_hasXweight_SelectP_raw_weightX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.and(
                        __.has(T.label, "hasCoin"),
                        __.has("material", "gold"),
                        __.has("weight", 30))))
                .has("material", SelectP.raw("material"))
                .has("weight", SelectP.raw("weight"))
                .inV().toList();
        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("material").equals("gold")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("weight").equals(30)));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_d001XXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, "d001")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).inV().toList();
        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXid_d001X_hasXlabel_DragonXXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(
                __.has(T.id, "d001"),
                __.has(T.label, "Dragon"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).inV().toList();

        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_d007XXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, "d007")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).inV().toList();
        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXid_d007X_hasXlabel_DragonXXXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(
                __.has(T.id, "d007"),
                __.has(T.label, "Dragon"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).inV().toList();

        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_within_d001_d002XXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, P.within("d001", "d002"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).inV().toList();
        Assert.assertEquals(6, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXid_within_d001_d007XXX_outE_hasXconstraint_byXhasXlabel_hasCoinXXX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.id, P.within("d001", "d007"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasCoin"))).inV().toList();
        Assert.assertEquals(6, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Coin")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_FireXXX() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Fire"))).toList();
        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Fire")));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_FireXXX_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Fire")))
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).outV().toList();

        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(10, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertTrue(Stream.ofAll(vertices).groupBy(Element::id).map(grouping -> grouping._2().size()).filter(size -> size != 3).isEmpty());
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_FireXXX_outE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Fire")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).outV().toList();

        Assert.assertEquals(0, vertices.size());
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_FireX_hasXduration_lt_100XXXX_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Fire"), __.has("duration", P.lt(100)))))
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire")))
                .outV().toList();

        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(1, Stream.ofAll(vertices).distinctBy(Element::id).size());
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXhasXlabel_hasOutFireXXX_inV_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Dragon"), __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).outV().toList();

        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(1, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertEquals("d000", Stream.ofAll(vertices).distinctBy(Element::id).get(0).id().toString());
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXhasXlabel_hasOutFireXXX_inV_inE_hasXconstraint_byXhasXlabel_hasInFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Dragon"), __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasInFire"))).outV().toList();

        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(3, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> Arrays.asList("d000", "d001", "d002").contains(vertex.id().toString())));
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXhasXlabel_hasInFireXXX_inV_inE_hasXconstraint_byXhasXlabel_hasInFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Dragon"), __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasInFire"))).inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasInFire"))).outV().toList();

        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(1, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertEquals("d000", Stream.ofAll(vertices).distinctBy(Element::id).get(0).id().toString());
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXhasXlabel_hasInFireXXX_inV_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Dragon"), __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasInFire"))).inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).outV().toList();

        Assert.assertEquals(3, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(3, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> Arrays.asList("d000", "d008", "d009").contains(vertex.id().toString())));
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXandXhasXlabel_hasOutFireX_hasXduration_0XXXX_inV_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Dragon"), __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.and(
                        __.has(T.label, "hasOutFire"),
                        __.has("duration", 0))))
                .inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).outV().toList();

        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(1, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertEquals("d000", Stream.ofAll(vertices).distinctBy(Element::id).get(0).id().toString());
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXandXhasXlabel_hasInFireX_hasXduration_0XXXX_inV_inE_hasXconstraint_byXhasXlabel_hasInFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Dragon"), __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.and(
                        __.has(T.label, "hasInFire"),
                        __.has("duration", 0))))
                .inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasInFire"))).outV().toList();

        Assert.assertEquals(1, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(1, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertEquals("d000", Stream.ofAll(vertices).distinctBy(Element::id).get(0).id().toString());
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXandXhasXlabel_hasInFireX_hasXduration_0XXXX_inV_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Dragon"), __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.and(
                        __.has(T.label, "hasInFire"),
                        __.has("duration", P.gt(0)))))
                .inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).outV().toList();

        Assert.assertEquals(2, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(2, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> Arrays.asList("d008", "d009").contains(vertex.id().toString())));
    }

    @Test
    public void g_V_hasXconstraint_byXhasXlabel_DragonXXX_outE_hasXconstraint_byXhasXlabel_hasOutFireXXX_hasXduration_selectP_directionX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire")))
                       .has("duration", SelectP.raw("duration"))
                .inV().toList();

        Assert.assertEquals(30, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Fire")));
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.value("duration") != null));
    }

    @Test
    public void g_V_hasXconstraint_byXandXhasXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXhasXlabel_hasFireXXX_inV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(
                __.has(T.label, "Dragon"),
                __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasFire")))
                .inV().toList();

        Assert.assertEquals(6, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Fire")));
    }

    @Test
    public void g_V_hasXxonstraintXhasXandXlabel_DragonX_hasXid_d000XXXX_outE_hasXconstraint_byXhasXlabel_hasFireXXX_inV_inE_hasXconstraint_byXhasXlabel_hasOutFireXXX_outV() throws InterruptedException {
        List<Vertex> vertices = g.V().has(CONSTRAINT, Constraint.by(__.and(
                __.has(T.label, "Dragon"),
                __.has(T.id, "d000"))))
                .outE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasFire"))).inV()
                .inE().has(CONSTRAINT, Constraint.by(__.has(T.label, "hasOutFire"))).outV().toList();

        Assert.assertEquals(6, vertices.size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> vertex.label().equals("Dragon")));
        Assert.assertEquals(3, Stream.ofAll(vertices).distinctBy(Element::id).size());
        Assert.assertTrue(Stream.ofAll(vertices).forAll(vertex -> Arrays.asList("d000", "d008", "d009").contains(vertex.id().toString())));
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
                                        "dragonId",
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("faction")
                                        )),
                                        Optional.of(new IndexPartitions.Impl("_id", coinPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Coin"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("material", "material", "string"),
                                                new GraphRedundantPropertySchema.Impl("weight", "weight", "int")),
                                        Optional.empty(),
                                        Optional.of(new IndexPartitions.Impl("dragonId", coinPartitions)))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList()),
                        new GraphEdgeSchema.Impl(
                                "hasOutFire",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "FireDual"), __.has("direction", Direction.OUT.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityAId",
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "fireId",
                                        Optional.of("Fire"),
                                        Collections.singletonList(new GraphRedundantPropertySchema.Impl("duration", "duration", "int")))),
                                Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasOutFire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireSingular")),
                                Optional.of(new GraphEdgeSchema.End.Impl("entityAId", Optional.of("Dragon"))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Fire"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(new IndexPartitions.Impl("_id", firePartitions)))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.destination).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInFire",
                                new GraphElementConstraint.Impl(__.and(__.has(T.label, "FireDual"), __.has("direction", Direction.IN.toString().toLowerCase()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityAId",
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "fireId",
                                        Optional.of("Fire"),
                                        Collections.singletonList(new GraphRedundantPropertySchema.Impl("duration", "duration", "int")))),
                                Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasInFire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireSingular")),
                                Optional.of(new GraphEdgeSchema.End.Impl("entityBId", Optional.of("Dragon"))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "_id",
                                        Optional.of("Fire"),
                                        Collections.emptyList(),
                                        Optional.empty(),
                                        Optional.of(new IndexPartitions.Impl("_id", firePartitions)))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.destination).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "hasFire",
                                new GraphElementConstraint.Impl(__.has(T.label, "FireDual")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "entityAId",
                                        Optional.of("Dragon"),
                                        Collections.emptyList(),
                                        Optional.of(new GraphElementRouting.Impl(
                                                new GraphElementPropertySchema.Impl("_id", "string"))),
                                        Optional.of(new IndexPartitions.Impl("_id", dragonPartitions)))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        "fireId",
                                        Optional.of("Fire"),
                                        Collections.singletonList(new GraphRedundantPropertySchema.Impl("duration", "duration", "int")))),
                                Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.emptyList(),
                                Stream.of(GraphEdgeSchema.Application.source).toJavaSet())
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

                fireEvent1.put("entityAId", sourceDragonId);
                fireEvent1.put("entityBId", destDragonId);
                fireEvent1.put("direction", Direction.OUT.toString().toLowerCase());
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
    //endregion

    //region Fields
    private GraphTraversalSource g;
    //endregion
}
