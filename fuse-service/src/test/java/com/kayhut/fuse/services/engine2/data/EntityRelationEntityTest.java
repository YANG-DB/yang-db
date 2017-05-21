package com.kayhut.fuse.services.engine2.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.gta.strategy.utils.ConverstionUtil;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.test.framework.index.MappingElasticConfigurer;
import com.kayhut.test.framework.index.Mappings;
import com.kayhut.test.framework.index.Mappings.Mapping;
import com.kayhut.test.framework.index.Mappings.Mapping.Property;
import com.kayhut.test.framework.index.Mappings.Mapping.Property.Index;
import com.kayhut.test.framework.index.Mappings.Mapping.Property.Type;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.idProvider.PromiseEdgeIdProvider;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jooby.test.JoobyRule;
import org.junit.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.kayhut.fuse.model.query.Constraint.*;


/**
 * Created by Roman on 11/05/2017.
 */
public class EntityRelationEntityTest {
    @ClassRule
    public static JoobyRule createApp() {
        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine2.dev.conf"));
    }

    @BeforeClass
    public static void setup() throws Exception {
        fuseClient = new FuseClient("/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/ontology/Dragons"));

        String idField = "id";

        elasticEmbeddedNode = new ElasticEmbeddedNode(
                new MappingElasticConfigurer("person", new Mappings().addMapping("Person", getPersonMapping())),
                new MappingElasticConfigurer("dragon", new Mappings().addMapping("Dragon", getDragonMapping())),
                new MappingElasticConfigurer(Arrays.asList("fire20170511", "fire20170512", "fire20170513"),
                        new Mappings().addMapping("Fire", getFireMapping())));

        birthDateValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        timestampValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        temperatureValueFunction = i -> 1000 + (100 * i);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "person",
                "Person",
                idField,
                () -> createPeople(10)).populate();

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "dragon",
                "Dragon",
                idField,
                () -> createDragons(10, birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L)))
                .populate(); // date interval is ~ 1 month

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "fire20170511",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-11 00:00:00").getTime()).apply(1200000L),
                        temperatureValueFunction))
                .populate(); // date interval is 20 min

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "fire20170512",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-12 00:00:00").getTime()).apply(600000L),
                        temperatureValueFunction))
                .populate(); // date interval is 10 min

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "fire20170513",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-13 00:00:00").getTime()).apply(300000L),
                        temperatureValueFunction))
                .populate(); // date interval is 5 min


        Thread.sleep(2000);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (elasticEmbeddedNode != null) {
            elasticEmbeddedNode.close();
            Thread.sleep(2000);
        }
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    //region Tests
    @Test
    public void test_Dragon_Fire_Dragon() throws Exception {
        Query query = Query.Builder.instance().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Dragon"), 2, 0),
                new Rel(2, $ont.rType$("Fire"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Dragon"), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))),
                allAssignments));
    }

    @Test
    public void test_Dragon_Fire_d1() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d1", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d2() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d2", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d3() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d3", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d4() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d4", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d5() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d5", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d6() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d6", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d7() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d7", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d8() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d8", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_d9() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d9", Rel.Direction.R);
    }

    @Test
    public void test_d0_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d0", Rel.Direction.R);
    }

    @Test
    public void test_d1_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d1", Rel.Direction.R);
    }

    @Test
    public void test_d2_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d2", Rel.Direction.R);
    }

    @Test
    public void test_d3_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d3", Rel.Direction.R);
    }

    @Test
    public void test_d4_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d4", Rel.Direction.R);
    }

    @Test
    public void test_d5_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d5", Rel.Direction.R);
    }

    @Test
    public void test_d6_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d6", Rel.Direction.R);
    }

    @Test
    public void test_d7_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d7", Rel.Direction.R);
    }

    @Test
    public void test_d8_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d8", Rel.Direction.R);
    }

    @Test
    public void test_d9_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d9", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon() throws Exception {
        Query query = Query.Builder.instance().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Dragon"), 2, 0),
                new Rel(2, $ont.rType$("Fire"), Rel.Direction.L, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Dragon"), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.L,
                Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.IN))),
                allAssignments));
    }

    @Test
    public void test_Dragon_FiredBy_d1() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d1", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d2() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d2", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d3() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d3", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d4() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d4", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d5() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d5", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d6() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d6", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d7() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d7", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d8() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d8", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_d9() throws Exception {
        test_Dragon_Fire_ConcreteDragon("d9", Rel.Direction.L);
    }

    @Test
    public void test_d0_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d0", Rel.Direction.L);
    }

    @Test
    public void test_d1_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d1", Rel.Direction.L);
    }

    @Test
    public void test_d2_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d2", Rel.Direction.L);
    }

    @Test
    public void test_d3_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d3", Rel.Direction.L);
    }

    @Test
    public void test_d4_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d4", Rel.Direction.L);
    }

    @Test
    public void test_d5_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d5", Rel.Direction.L);
    }

    @Test
    public void test_d6_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d6", Rel.Direction.L);
    }

    @Test
    public void test_d7_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d7", Rel.Direction.L);
    }

    @Test
    public void test_d8_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d8", Rel.Direction.L);
    }

    @Test
    public void test_d9_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d9", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_Fire_temperature_eq_1000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.eq, 1000);
    }

    @Test
    public void test_Dragon_Fire_temperature_le_1000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.le, 1000);
    }

    @Test
    public void test_Dragon_Fire_temperature_lt_1000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.lt, 1000);
    }

    @Test
    public void test_Dragon_Fire_temperature_gt_1000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.gt, 1000);
    }

    @Test
    public void test_Dragon_Fire_temperature_ge_1000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.ge, 1000);
    }

    @Test
    public void test_Dragon_Fire_temperature_inRange_1000_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.inRange, Arrays.asList(1000, 1500));
    }

    @Test
    public void test_Dragon_Fire_temperature_notInRange_1000_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.notInRange, Arrays.asList(1000, 1500));
    }

    @Test
    public void test_Dragon_Fire_temperature_ne_1000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.ne, 1000);
    }

    @Test
    public void test_Dragon_Fire_temperature_inSet_1000_1100_1200_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.inSet, Arrays.asList(1000, 1100, 1200));
    }

    @Test
    public void test_Dragon_Fire_temperature_not_inSet_1000_1100_1200_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.notInSet, Arrays.asList(1000, 1100, 1200));
    }

    @Test
    public void test_Dragon_Fire_temperature_eq_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.eq, 1500);
    }

    @Test
    public void test_Dragon_Fire_temperature_le_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.le, 1500);
    }

    @Test
    public void test_Dragon_Fire_temperature_lt_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.lt, 1500);
    }

    @Test
    public void test_Dragon_Fire_temperature_gt_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.gt, 1500);
    }

    @Test
    public void test_Dragon_Fire_temperature_ge_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.ge, 1500);
    }

    @Test
    public void test_Dragon_Fire_temperature_inRange_1500_2000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.inRange, Arrays.asList(1500, 2000));
    }

    @Test
    public void test_Dragon_Fire_temperature_notInRange_1500_2000_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.notInRange, Arrays.asList(1500, 2000));
    }

    @Test
    public void test_Dragon_Fire_temperature_ne_1500_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.ne, 1500);
    }

    @Test
    public void test_Dragon_Fire_temperature_inSet_1500_1600_1700_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.inSet, Arrays.asList(1500, 1600, 1700));
    }

    @Test
    public void test_Dragon_Fire_temperature_not_inSet_1500_1600_1700_Dragon() throws Exception {
        test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp.notInSet, Arrays.asList(1500, 1600, 1700));
    }

    @Test
    public void test_Dragon_birthDate_eq_1980_03_01_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.eq, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_birthDate_gt_1980_03_01_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.gt, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_birthDate_ge_1980_03_01_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.ge, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_birthDate_lt_1980_03_01_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.lt, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_birthDate_le_1980_03_01_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.le, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_birthDate_inRange_1980_03_01_05_01_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.inRange,
                Arrays.asList(sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-05-01 00:00:00").getTime()));
    }

    @Test
    public void test_Dragon_birthDate_notInRange_1980_03_01_05_01_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.notInRange,
                Arrays.asList(sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-05-01 00:00:00").getTime()));
    }

    @Test
    public void test_Dragon_birthDate_inSet_1980_03_01_31_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.inSet,
                Arrays.asList(
                        sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-03-31 00:00:00").getTime()));
    }

    @Test
    public void test_Dragon_birthDate_notInSet_1980_03_01_31_Fire_Dragon() throws Exception {
        test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp.notInSet,
                Arrays.asList(
                        sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-03-31 00:00:00").getTime()));
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_eq_1980_03_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.eq, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_gt_1980_03_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.gt, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_ge_1980_03_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.ge, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_lt_1980_03_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.lt, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_le_1980_03_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.le, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_inRange_1980_03_01_05_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.inRange,
                Arrays.asList(sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-05-01 00:00:00").getTime()));
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_notInRange_1980_03_01_05_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.notInRange,
                Arrays.asList(sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-05-01 00:00:00").getTime()));
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_ne_1980_03_01() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.ne, sdf.parse("1980-03-01 00:00:00").getTime());
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_inSet_1980_03_01_31() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.inSet,
                Arrays.asList(
                        sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-03-31 00:00:00").getTime()));
    }

    @Test
    public void test_Dragon_Fire_Dragon_birthDate_notInSet_1980_03_01_31() throws Exception {
        test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp.notInSet,
                Arrays.asList(
                        sdf.parse("1980-03-01 00:00:00").getTime(),
                        sdf.parse("1980-03-31 00:00:00").getTime()));
    }
    //endregion

    //region Protected Methods
    private static void test_Dragon_Fire_ConcreteDragon(String eId, Rel.Direction direction) throws Exception {
        Query query = Query.Builder.instance().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Dragon"), 2, 0),
                new Rel(2, $ont.rType$("Fire"), direction, null, 3, 0),
                new EConcrete(3, "B", $ont.eType$("Dragon"), eId, eId, 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                direction,
                Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION,
                                        direction == Rel.Direction.R ? Direction.OUT : Direction.IN))),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("B"))
                        .filter(entity -> entity.geteID().equals(eId))
                        .isEmpty()));
    }

    private static void test_ConcreteDragon_Fire_Dragon(String eId, Rel.Direction direction) throws Exception {
        Query query = Query.Builder.instance().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", $ont.eType$("Dragon"), eId, eId, 2, 0),
                new Rel(2, $ont.rType$("Fire"), direction, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Dragon"), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                direction,
                Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION,
                                        direction == Rel.Direction.R ? Direction.OUT : Direction.IN))),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("A"))
                        .filter(entity -> entity.geteID().equals(eId))
                        .isEmpty()));
    }

    private static void test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp op, Object value) throws Exception {
        Query query = Query.Builder.instance().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Dragon"), 2, 0),
                new Rel(2, $ont.rType$("Fire"), Rel.Direction.R, null, 4, 3),
                new RelProp(3, $ont.pType$("temperature").toString(), of(op, value), 0),
                new ETyped(4, "B", $ont.eType$("Dragon"), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                Constraint.by(__.and(
                        __.has(T.label, "Fire"),
                        __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT),
                        __.has("temperature", ConverstionUtil.convertConstraint(of(op, value))))),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("B"))
                        .map(entity -> Integer.parseInt(entity.geteID().substring(1)))
                        .filter(intId -> ConverstionUtil.convertConstraint(of(op, value))
                                .test(temperatureValueFunction.apply(intId)))
                        .isEmpty()));
    }

    private static void test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp op, Object value) throws Exception {
        Query query = Query.Builder.instance().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Dragon"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, $ont.pType$("birthDate").toString(), of(op, value)),
                new Rel(4, $ont.rType$("Fire"), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Dragon"), 0, 0)
        )).build();

        long startingDate = sdf.parse("1980-01-01 00:00:00").getTime();
        long interval = 2592000000L;

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                Constraint.by(__.and(
                        __.has(T.label, "Fire"),
                        __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("A"))
                        .map(entity -> Integer.parseInt(entity.geteID().substring(1)))
                        .filter(intId -> ConverstionUtil.convertConstraint(of(op, value))
                                .test(birthDateValueFunctionFactory.apply(startingDate).apply(interval).apply(intId)))
                        .isEmpty()));
    }

    private static void test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp op, Object value) throws Exception {
        Query query = Query.Builder.instance().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Dragon"), 2, 0),
                new Rel(2, $ont.rType$("Fire"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Dragon"), 4, 0),
                new EProp(4, $ont.pType$("birthDate").toString(), of(op, value))
        )).build();

        long startingDate = sdf.parse("1980-01-01 00:00:00").getTime();
        long interval = 2592000000L;

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                Constraint.by(__.and(
                        __.has(T.label, "Fire"),
                        __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("B"))
                        .map(entity -> Integer.parseInt(entity.geteID().substring(1)))
                        .filter(intId -> ConverstionUtil.convertConstraint(of(op, value))
                                .test(birthDateValueFunctionFactory.apply(startingDate).apply(interval).apply(intId)))
                        .isEmpty()));
    }

    private static void testAndAssertQuery(Query query, QueryResult expectedQueryResult) throws Exception {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        QueryResultAssert.assertEquals(expectedQueryResult, actualQueryResult);
    }

    private static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "p" + i);
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }

    private static Mapping getPersonMapping() {
        return new Mapping().addProperty("name", new Property(Type.string, Index.not_analyzed));
    }

    private static Iterable<Map<String, Object>> createDragons(
            int numDragons,
            Function<Integer, Long> birthDateValueFunction) {

        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "d" + i);
            dragon.put("name", "dragon" + i);
            dragon.put("birthDate", sdf.format(new Date(birthDateValueFunction.apply(i))));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Mapping getDragonMapping() {
        return new Mapping()
                .addProperty("name", new Property(Type.string, Index.not_analyzed))
                .addProperty("birthDate", new Property(Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time"));
    }


    private static Iterable<Map<String, Object>> createDragonFireDragonEdges(
            int numDragons,
            Function<Integer, Long> timestampValueFunction,
            Function<Integer, Integer> temperatureValueFunction
            ) throws ParseException {
        List<Map<String, Object>> fireEdges = new ArrayList<>();

        int counter = 0;
        for(int i = 0 ; i < numDragons ; i++) {
            for(int j = 0 ; j < i ; j++) {
                Map<String, Object> fireEdge = new HashMap<>();
                fireEdge.put("id", "fire" + counter);
                fireEdge.put("timestamp", timestampValueFunction.apply(counter));
                fireEdge.put("direction", Direction.OUT);
                fireEdge.put("temperature", temperatureValueFunction.apply(j));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", "fire" + counter + 1);
                fireEdgeDual.put("timestamp", timestampValueFunction.apply(counter));
                fireEdgeDual.put("direction", Direction.IN);

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "d" + i);
                entityAI.put("type", "Dragon");
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "d" + j);
                entityAJ.put("type", "Dragon");
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "d" + i);
                entityBI.put("type", "Dragon");
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "d" + j);
                entityBJ.put("type", "Dragon");

                fireEdge.put("entityA", entityAI);
                fireEdge.put("entityB", entityBJ);
                fireEdgeDual.put("entityA", entityAJ);
                fireEdgeDual.put("entityB", entityBI);

                fireEdges.addAll(Arrays.asList(fireEdge, fireEdgeDual));

                counter += 2;
            }
        }

        return fireEdges;
    }

    private static Mapping getFireMapping() {
        return new Mapping()
                .addProperty("timestamp", new Property(Type.date))
                .addProperty("direction", new Property(Type.string, Index.not_analyzed))
                .addProperty("temperature", new Property(Type.integer))
                .addProperty("entityA", new Property()
                    .addProperty("id", new Property(Type.string, Index.not_analyzed))
                    .addProperty("type", new Property(Type.string, Index.not_analyzed)))
                .addProperty("entityB", new Property()
                        .addProperty("id", new Property(Type.string, Index.not_analyzed))
                        .addProperty("type", new Property(Type.string, Index.not_analyzed)));
    }
    //endregion

    //region QueryResults
    private static QueryResult queryResult_Dragons_Fire_Dragon(
            int numDragons,
            Rel.Direction direction,
            TraversalConstraint constraint,
            Predicate<Assignment> assignmentPredicate) throws Exception {

        String eTag1 = direction == Rel.Direction.R ? "A" : "B";
        String eTag2 = direction == Rel.Direction.R ? "B" : "A";

        QueryResult.Builder builder = QueryResult.Builder.instance();
        PromiseEdgeIdProvider edgeIdProvider = new PromiseEdgeIdProvider(Optional.of(constraint));

        for(int i = 0 ; i < numDragons ; i++) {
            for (int j = 0; j < i; j++) {
                Entity entityA = Entity.Builder.instance()
                        .withEID("d" + i)
                        .withETag(Collections.singletonList(eTag1))
                        .withEType($ont.eType$("Dragon"))
                        .build();

                Entity entityB = Entity.Builder.instance()
                        .withEID("d" + j)
                        .withETag(Collections.singletonList(eTag2))
                        .withEType($ont.eType$("Dragon"))
                        .build();

                Relationship relationship = Relationship.Builder.instance()
                        .withRID(edgeIdProvider.get(GlobalConstants.Labels.PROMISE,
                                new PromiseVertex(
                                        Promise.as(direction == Rel.Direction.R ?
                                               entityA.geteID() :
                                               entityB.geteID()),
                                        Optional.empty(),
                                        null),
                                new PromiseVertex(
                                        Promise.as(direction == Rel.Direction.R ?
                                               entityB.geteID() :
                                               entityA.geteID()),
                                        Optional.empty(),
                                        null),
                                null))
                        .withDirectional(true)
                        .withEID1(entityA.geteID())
                        .withEID2(entityB.geteID())
                        .withETag1(entityA.geteTag().get(0))
                        .withETag2(entityB.geteTag().get(0))
                        .withRType($ont.rType$("Fire"))
                        .build();

                Assignment assignment = Assignment.Builder.instance().withEntity(entityA).withEntity(entityB)
                        .withRelationship(relationship).build();

                if (assignmentPredicate.test(assignment)) {
                    builder.withAssignment(assignment);
                }
            }
        }

        return builder.build();
    }
    //endregion

    //region Fields
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static FuseClient fuseClient;
    private static Ontology.Accessor $ont;
    private static SimpleDateFormat sdf;

    private static Function<Long, Function<Long, Function<Integer, Long>>> timestampValueFunctionFactory;
    private static Function<Long, Function<Long, Function<Integer, Long>>> birthDateValueFunctionFactory;
    private static Function<Integer, Integer> temperatureValueFunction;
    //endregion

    //region Predicates
    private static Predicate<Assignment> allAssignments = assignment -> true;
    //endregion
}
