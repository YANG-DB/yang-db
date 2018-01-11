package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.model.OntologyTestUtils.*;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.fuse.stat.StatCalculator;
import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.idProvider.HashEdgeIdProvider;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import com.kayhut.test.framework.index.MappingElasticConfigurer;
import com.kayhut.test.framework.index.Mappings;
import com.kayhut.test.framework.index.Mappings.Mapping;
import com.kayhut.test.framework.index.Mappings.Mapping.Property;
import com.kayhut.test.framework.index.Mappings.Mapping.Property.Index;
import com.kayhut.test.framework.index.Mappings.Mapping.Property.Type;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;


/**
 * Created by Roman on 11/05/2017.
 */
public abstract class EntityRelationEntityTest {
    public static void setup(TransportClient client) throws Exception {
        setup(client, false);
    }

    public static void setup(TransportClient client, boolean calcStats) throws Exception {
        fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));

        String idField = "id";

        new MappingElasticConfigurer(PERSON.name.toLowerCase(), new Mappings().addMapping("pge", getPersonMapping()))
                .configure(client);
        new MappingElasticConfigurer(DRAGON.name.toLowerCase(), new Mappings().addMapping("pge", getDragonMapping()))
                .configure(client);
        new MappingElasticConfigurer(Arrays.asList(
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513"),
                new Mappings().addMapping("pge", getFireMapping()))
                .configure(client);

        birthDateValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        timestampValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        temperatureValueFunction = i -> 1000 + (100 * i);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        new ElasticDataPopulator(
                client,
                PERSON.name.toLowerCase(),
                "pge",
                idField,
                () -> createPeople(10)).populate();

        new ElasticDataPopulator(
                client,
                DRAGON.name.toLowerCase(),
                "pge",
                idField,
                () -> createDragons(10, birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L)))
                .populate(); // date interval is ~ 1 month

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170511",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-11 00:00:00").getTime()).apply(1200000L),
                        temperatureValueFunction))
                .populate(); // date interval is 20 min

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170512",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-12 00:00:00").getTime()).apply(600000L),
                        temperatureValueFunction))
                .populate(); // date interval is 10 min

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170513",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-13 00:00:00").getTime()).apply(300000L),
                        temperatureValueFunction))
                .populate(); // date interval is 5 min


        client.admin().indices().refresh(new RefreshRequest(
                PERSON.name.toLowerCase(),
                DRAGON.name.toLowerCase(),
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513"
        )).actionGet();

        if(calcStats){
            StatCalculator.run(client, client, new StatConfiguration("statistics.test.properties").getInstance());
        }
    }

    public static void cleanup(TransportClient client) throws Exception {
        cleanup(client, false);
    }

    public static void cleanup(TransportClient client, boolean statsUsed) throws Exception {
        client.admin().indices()
                .delete(new DeleteIndexRequest(
                        PERSON.name.toLowerCase(),
                        DRAGON.name.toLowerCase(),
                        FIRE.getName().toLowerCase() + "20170511",
                        FIRE.getName().toLowerCase() + "20170512",
                        FIRE.getName().toLowerCase() + "20170513"))
                .actionGet();

        if(statsUsed){
            client.admin().indices().delete(new DeleteIndexRequest("stat")).actionGet();
        }
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    //region Tests
    @Test
    public void test_Dragon_Fire_Dragon() throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                getExpectedEdgeTraversalConstraint(FIRE.getName(), Direction.OUT, null, null, null, singleton(DRAGON.name)),
                allAssignments));
    }

    @Test
    public void test_Dragon_Fire_Dragon_1() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_1", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_2() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_2", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_3() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_3", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_4() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_4", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_5() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_5", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_6() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_6", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_7() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_7", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_8() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_8", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_Fire_Dragon_9() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_9", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_0_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_0", Rel.Direction.R);
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
    public void test_Dragon_3_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_3", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_4_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_4", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_5_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_5", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_6_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_6", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_7_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_7", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_8_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_8", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_9_Fire_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_9", Rel.Direction.R);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon() throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), Rel.Direction.L, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.L,
                getExpectedEdgeTraversalConstraint(FIRE.getName(), Direction.IN, null, null, null, singleton(DRAGON.name)),
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
    public void test_Dragon_FiredBy_Dragon_3() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_3", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon_4() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_4", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon_5() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_5", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon_6() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_6", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon_7() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_7", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon_8() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_8", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_FiredBy_Dragon_9() throws Exception {
        test_Dragon_Fire_ConcreteDragon("Dragon_9", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_0_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_0", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_1_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_1", Rel.Direction.L);
    }

    @Test
    public void test_d2_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("d2", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_3_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_3", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_4_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_4", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_5_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_5", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_6_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_6", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_7_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_7", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_8_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_8", Rel.Direction.L);
    }

    @Test
    public void test_Dragon_9_FiredBy_Dragon() throws Exception {
        test_ConcreteDragon_Fire_Dragon("Dragon_9", Rel.Direction.L);
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

    @Test
    public void test_Dragon_Fire_Untyped() throws Exception {
        test_Dragon_Fire_Untyped(Rel.Direction.R);
    }

    @Test
    public void test_Dragon_FiredBy_Untyped() throws Exception {
        test_Dragon_Fire_Untyped(Rel.Direction.L);
    }

    @Test
    public void test_Untyped_Fire_Dragon() throws Exception {
        test_Untyped_Fire_Dragon(Rel.Direction.R);
    }

    @Test
    public void test_Untyped_FiredBy_Dragon() throws Exception {
        test_Untyped_Fire_Dragon(Rel.Direction.L);
    }
    //endregion

    //region Protected Methods
    protected boolean shouldIgnoreRelId() {
        return false;
    }

    private void test_Dragon_Fire_ConcreteDragon(String eId, Rel.Direction direction) throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), direction, null, 3, 0),
                new EConcrete(3, "B", $ont.eType$(DRAGON.name), eId, eId, singletonList(NAME.type), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                direction,
                getExpectedEdgeTraversalConstraint(
                        FIRE.getName(),
                        direction == Rel.Direction.R ? Direction.OUT : Direction.IN,
                        null,
                        null,
                        eId,
                        singleton(DRAGON.name)),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("B"))
                        .filter(entity -> entity.geteID().equals(eId))
                        .isEmpty()));
    }

    private void test_ConcreteDragon_Fire_Dragon(String eId, Rel.Direction direction) throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", $ont.eType$(DRAGON.name), eId, eId, singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), direction, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                direction,
                getExpectedEdgeTraversalConstraint(
                        FIRE.getName(),
                        direction == Rel.Direction.R ? Direction.OUT : Direction.IN,
                        null,
                        null,
                        null,
                        singleton(DRAGON.name)),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("A"))
                        .filter(entity -> entity.geteID().equals(eId))
                        .isEmpty()));
    }

    private void test_Dragon_Fire_temperature_op_value_Dragon(ConstraintOp op, Object value) throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 4, 3),
                new RelProp(3, $ont.pType$(TEMPERATURE.name).toString(), of(op, value), 0),
                new ETyped(4, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                getExpectedEdgeTraversalConstraint(
                        FIRE.getName(),
                        Direction.OUT,
                        TEMPERATURE.name,
                        ConversionUtil.convertConstraint(of(op, value)),
                        null,
                        singleton(DRAGON.name)),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("B"))
                        .map(entity -> Integer.parseInt(entity.geteID().substring("Dragon_".length())))
                        .filter(intId -> ConversionUtil.convertConstraint(of(op, value))
                                .test(temperatureValueFunction.apply(intId)))
                        .isEmpty()));
    }

    private void test_Dragon_birthDate_op_value_Fire_Dragon(ConstraintOp op, Object value) throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(NAME.type), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, $ont.pType$(BIRTH_DATE.name).toString(), of(op, value)),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();

        long startingDate = sdf.parse("1980-01-01 00:00:00").getTime();
        long interval = 2592000000L;

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                getExpectedEdgeTraversalConstraint(FIRE.getName(), Direction.OUT, null, null, null, singleton(DRAGON.name)),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("A"))
                        .map(entity -> Integer.parseInt(entity.geteID().substring("Dragon_".length())))
                        .filter(intId -> ConversionUtil.convertConstraint(of(op, value))
                                .test(birthDateValueFunctionFactory.apply(startingDate).apply(interval).apply(intId)))
                        .isEmpty()));
    }

    private void test_Dragon_Fire_Dragon_birthDate_op_value(ConstraintOp op, Object value) throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 4, 0),
                new EProp(4, $ont.pType$(BIRTH_DATE.name), of(op, value))
        )).build();

        long startingDate = sdf.parse("1980-01-01 00:00:00").getTime();
        long interval = 2592000000L;

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                Rel.Direction.R,
                getExpectedEdgeTraversalConstraint(FIRE.getName(), Direction.OUT, null, null, null, singleton(DRAGON.name)),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("B"))
                        .map(entity -> Integer.parseInt(entity.geteID().substring("Dragon_".length())))
                        .filter(intId -> ConversionUtil.convertConstraint(of(op, value))
                                .test(birthDateValueFunctionFactory.apply(startingDate).apply(interval).apply(intId)))
                        .isEmpty()));
    }

    private void test_Dragon_Fire_Untyped(Rel.Direction direction) throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), direction, null, 3, 0),
                new EUntyped(3, "B", singletonList(NAME.type), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                direction,
                getExpectedEdgeTraversalConstraint(
                        FIRE.getName(),
                        direction == Rel.Direction.R ? Direction.OUT : Direction.IN,
                        null,
                        null,
                        null,
                        Arrays.asList(PERSON.name, HORSE.name, DRAGON.name, KINGDOM.name, GUILD.name)),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("A"))
                        .filter(entity -> entity.geteType() == $ont.eType$(DRAGON.name))
                        .isEmpty()));
    }

    private void test_Untyped_Fire_Dragon(Rel.Direction direction) throws Exception {
        Query query = Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EUntyped(1, "A", singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), direction, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();

        testAndAssertQuery(query, queryResult_Dragons_Fire_Dragon(
                10,
                direction,
                getExpectedEdgeTraversalConstraint(
                        FIRE.getName(),
                        direction == Rel.Direction.R ? Direction.OUT : Direction.IN,
                        null,
                        null,
                        null,
                        singleton(DRAGON.name)),
                assignment -> !Stream.ofAll(assignment.getEntities())
                        .filter(entity -> entity.geteTag().contains("B"))
                        .filter(entity -> entity.geteType() == $ont.eType$(DRAGON.name))
                        .isEmpty()));
    }

    protected abstract TraversalConstraint getExpectedEdgeTraversalConstraint(
            String relationType,
            Direction direction,
            String relProperty,
            P relPropertyPredicate,
            String entityBId,
            Iterable<String> entityBTypes);

    private void testAndAssertQuery(Query query, QueryResult expectedQueryResult) throws Exception {
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
        QueryResultAssert.assertEquals(expectedQueryResult, actualQueryResult, shouldIgnoreRelId());
    }



    private static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "Person_" + i);
            person.put("type", "Person");
            person.put(NAME.name, "person" + i);
            people.add(person);
        }
        return people;
    }

    private static Mapping getPersonMapping() {
        return new Mapping()
                .addProperty("type", new Property(Type.keyword))
                .addProperty(NAME.name, new Property(Type.keyword));
    }

    private static Iterable<Map<String, Object>> createDragons(
            int numDragons,
            Function<Integer, Long> birthDateValueFunction) {

        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "Dragon_" + i);
            dragon.put("type", DRAGON.name);
            dragon.put(NAME.name, DRAGON.name + i);
            dragon.put(BIRTH_DATE.name, sdf.format(new Date(birthDateValueFunction.apply(i))));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Mapping getDragonMapping() {
        return new Mapping()
                .addProperty("type", new Property(Type.keyword))
                .addProperty(NAME.name, new Property(Type.keyword))
                .addProperty(BIRTH_DATE.name, new Property(Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"));
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
                fireEdge.put("id", FIRE.getName() + counter);
                fireEdge.put("type", FIRE.getName());
                fireEdge.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdge.put("direction", Direction.OUT);
                fireEdge.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", FIRE.getName() + counter + 1);
                fireEdgeDual.put("type", FIRE.getName());
                fireEdgeDual.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdgeDual.put("direction", Direction.IN);
                fireEdgeDual.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "Dragon_" + i);
                entityAI.put("type", DRAGON.name);
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "Dragon_" + j);
                entityAJ.put("type", DRAGON.name);
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "Dragon_" + i);
                entityBI.put("type", DRAGON.name);
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "Dragon_" + j);
                entityBJ.put("type", DRAGON.name);

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
                .addProperty("type", new Property(Type.keyword))
                .addProperty(TIMESTAMP.name, new Property(Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"))
                .addProperty("direction", new Property(Type.keyword))
                .addProperty(TEMPERATURE.name, new Property(Type.integer))
                .addProperty("entityA", new Property()
                    .addProperty("id", new Property(Type.keyword))
                    .addProperty("type", new Property(Type.keyword)))
                .addProperty("entityB", new Property()
                        .addProperty("id", new Property(Type.keyword))
                        .addProperty("type", new Property(Type.keyword)));
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
        HashEdgeIdProvider edgeIdProvider = new HashEdgeIdProvider(Optional.of(constraint));

        for(int i = 0 ; i < numDragons ; i++) {
            for (int j = 0; j < i; j++) {
                Entity entityA = Entity.Builder.instance()
                        .withEID("Dragon_" + i)
                        .withETag(singletonList(eTag1))
                        .withEType($ont.eType$(DRAGON.name))
                        .withProperties(singletonList(
                                new com.kayhut.fuse.model.results.Property(NAME.type, "raw", DRAGON.name + i)))
                        .build();

                Entity entityB = Entity.Builder.instance()
                        .withEID("Dragon_" + j)
                        .withETag(singletonList(eTag2))
                        .withEType($ont.eType$(DRAGON.name))
                        .withProperties(singletonList(
                                new com.kayhut.fuse.model.results.Property(NAME.type, "raw", DRAGON.name + j)))
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
                        .withRType($ont.rType$(FIRE.getName()))
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
