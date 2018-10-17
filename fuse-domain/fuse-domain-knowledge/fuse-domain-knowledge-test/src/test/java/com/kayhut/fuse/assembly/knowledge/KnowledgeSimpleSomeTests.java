package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.RefBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultAssert;
import com.kayhut.fuse.model.results.QueryResultBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import static com.kayhut.fuse.assembly.knowledge.Setup.client;
import static com.kayhut.fuse.assembly.knowledge.Setup.fuseClient;
import static com.kayhut.fuse.assembly.knowledge.Setup.manager;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder._ref;
import static com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


public class KnowledgeSimpleSomeTests {

    static KnowledgeWriterContext ctx;
    static ValueBuilder ev1, ev2, ev3, ev4, ev5;
    static EntityBuilder e1, e2, e3, e4;
    static RefBuilder ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8, ref9;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Entities for tests
        e1 = _e(ctx.nextLogicalId()).cat("opel").ctx("context1").creationTime(sdf.parse("2018-01-28 14:33:53.567"))
                .lastUpdateTime(sdf.parse("2014-12-20 12:17:47.791")).deleteTime(sdf.parse("2018-07-09 02:02:02.222"))
                .lastUpdateUser("Kobi").creationUser("Dudi Haim");
        e2 = _e(ctx.nextLogicalId()).cat("opel").ctx("context2").lastUpdateTime(sdf.parse("2017-03-20 12:12:35.111"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222")).creationTime(sdf.parse("2012-04-27 19:38:33.797"))
                .lastUpdateUser("Yael").creationUser("Yael Gabai");
        e3 = _e(ctx.nextLogicalId()).cat("citroen").ctx("context3").lastUpdateUser("Avi Bucavza").creationUser("Yael Gabai")
                .deleteTime(sdf.parse("2018-02-09 02:02:02.222")).lastUpdateTime(sdf.parse("2017-03-20 12:12:35.111"))
                .creationTime(sdf.parse("2012-04-27 19:38:33.797"));
        e4 = _e(e3.logicalId).cat("mazda").ctx("context1").creationUser("Dudi Frid").lastUpdateUser("Avi Bucavza")
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222")).lastUpdateTime(sdf.parse("2025-01-11 23:22:25.221"))
                .creationTime(sdf.parse("2000-01-20 10:08:03.001"));
        // Evalue entities for tests
        ev1 = _v(ctx.nextValueId()).field("Car sale").value("Chevrolet").bdt("identifier").ctx("sale")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        ev2 = _v(ctx.nextValueId()).field("Garage").value("Zion and his sons").bdt("identifier").ctx("fixing cars")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu Peretz").creationTime(new Date(System.currentTimeMillis()));
        ev3 = _v(ctx.nextValueId()).field("Car sale").value("chevrolet").bdt("California").ctx("Sale cars")
                .creationUser("Kobi Peretz").lastUpdateUser("Dudu Shaul").creationTime(new Date(System.currentTimeMillis()));
        ev4 = _v(ctx.nextValueId()).field("Garage").value(322).bdt("Netanya").ctx("fixing cars").creationUser("Haim Melamed")
                .lastUpdateUser("haim Melamed").creationTime(sdf.parse("2018-04-17 13:05:13.098"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987"));
        ev5 = _v(ctx.nextValueId()).field("garage").value("White Building").bdt("Identifier").ctx("colors")
                .creationUser("Haim Melamed").creationTime(sdf.parse("2016-09-02 19:45:23.123"))
                .lastUpdateUser("haim Melamed").deleteTime(sdf.parse("2017-12-12 01:00:00.000"));
        // References for tests
        ref1 = _ref(ctx.nextRefId()).title("steering wheel").content("size in cm").url("http://www.hadarrosen.com")
                .sys("HR").creationUser("Haim hania").creationTime(sdf.parse("2010-12-01 12:24:36.489"))
                .lastUpdateUser("dudi Fargon").lastUpdateTime(sdf.parse("2017-01-01 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        ref2 = _ref(ctx.nextRefId()).title("Engine").content("Quantity of parts").url("https://www.carsforum.co.il")
                .sys("CARS FORUM").creationUser("Kobi Terminator David").creationTime(sdf.parse("2000-01-01 02:04:30.480"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2015-12-30 11:40:06.057"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.021"));
        ref3 = _ref(ctx.nextRefId()).title("Steering Wheel").content("Size in CM").url("http://www.hadarrosen.com")
                .sys("Hr").creationUser("Haim Hania").creationTime(sdf.parse("2010-12-01 12:24:36.489"))
                .lastUpdateUser("Fargon").lastUpdateTime(sdf.parse("2017-11-27 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        ref4 = _ref(ctx.nextRefId()).title("Gearbox").content("Quantity of parts").url("http://www.musach.co.il/forum")
                .sys("Cars Forum").creationUser("Dudi hania").creationTime(sdf.parse("2001-7-07 07:27:37.477"))
                .lastUpdateUser("Gabi Fargon").lastUpdateTime(sdf.parse("2014-04-07 07:37:57.070"))
                .deleteTime(sdf.parse("2016-06-06 11:17:17.170"));
        ref5 = _ref(ctx.nextRefId()).title("engine").content("Quantity of parts").url("https://www.carsforum.co.il")
                .sys("High Risk").creationUser("Avi Zion Aharon Moshe").creationTime(sdf.parse("2010-12-01 12:24:36.489"))
                .lastUpdateUser("avi moshe").lastUpdateTime(sdf.parse("2014-04-07 07:37:57.070"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        ref6 = _ref(ctx.nextRefId()).title("Brassieres").content("size in Cm").url("http://www.rechev.net")
                .sys("cars").creationUser("Yariv").creationTime(sdf.parse("2019-10-10 10:10:10.100"))
                .lastUpdateUser("Yariv").lastUpdateTime(sdf.parse("2022-12-31 15:34:56.000"))
                .deleteTime(sdf.parse("2023-02-03 11:11:11.022"));
        ref7 = _ref(ctx.nextRefId()).title("Brassieres").content("Size in MM").url("http://www.rechev.net")
                .sys("Brassieres").creationUser("Yariv Aaaav").creationTime(sdf.parse("2019-10-10 10:10:10.101"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2017-08-08 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        ref8 = _ref(ctx.nextRefId()).title("cover chairs").content("size").url("http://www.chaircovers.com")
                .sys("Chair").creationUser("Haim hania").creationTime(sdf.parse("1999-12-06 12:24:36.666"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2022-12-31 15:34:56.000"))
                .deleteTime(sdf.parse("2023-02-03 11:11:11.022"));
        ref9 = _ref(ctx.nextRefId()).title("Engine").content("Quantity of parts").url("https://www.carsforum.co.il")
                .sys("CARS FORUM").creationUser("Kobi Terminator David").creationTime(sdf.parse("2000-01-01 02:04:30.480"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2015-12-30 11:40:06.057"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.021"));
        // Add Relation between Entities, Evalues and References
        e1.value(ev1.reference(ref1));
        e2.value(ev2.reference(ref2).reference(ref3));
        e3.value(ev3.reference(ref4).reference(ref5).reference(ref6));
        e4.value(ev4.reference(ref7), ev5.reference(ref8)).reference(ref9);
        // Insert Relation and Rvalue entities to ES
        Assert.assertEquals(4, commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals(5, commit(ctx, INDEX, ev1, ev2, ev3, ev4, ev5));
        Assert.assertEquals(9, commit(ctx, REF_INDEX, ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8, ref9));
    }


    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper


    // Start Tests:
    @Test
    public void testOneSomeWithPropAndRel() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)), // 1,2
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.some, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev1.fieldId)), // 1
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 0, 0) // 1,2,3
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(ev1.toEntity()).withEntity(ev2.toEntity())
                        .withEntity(ref1.toEntity()).withEntity(ref2.toEntity()).withEntity(ref3.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        //.withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev1.withRelations("hasEvalueReference", ref1.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testOneSomeWithTwoProps() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e3.category)), // 3
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.some, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev3.fieldId)), // 3
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 10, 0), // 1,2,3
                        new EProp(10, "content", Constraint.of(ConstraintOp.eq, ref4.content))  // 4,5
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity()).withEntity(ev3.toEntity())
                        .withEntity(ref4.toEntity()).withEntity(ref5.toEntity())
                        .withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        //.withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref4.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testOneSomeWithTwoRels() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList(3, 4), 0),

                            new Rel(3, "hasEntityReference", R, null, 13, 0), // e4, ref9
                                new ETyped(13, "R", "Reference", 0, 0),
                            // OR
                            new Rel(4, "hasEvalue", R, null, 5, 0),
                                new ETyped(5, "R1", "Evalue", 6, 0),
                                new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                                new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev3.fieldId)), // e1, ev1, ref1 + e3,ev3, ref4,5,6
                                // AND
                                    new Rel(8, "hasEvalueReference", R, null, 9, 0),
                                    new ETyped(9, "R", "Reference", 0, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e3.toEntity()).withEntity(e4.toEntity())
                        .withEntity(ev1.toEntity()).withEntity(ev3.toEntity())
                        .withEntity(ref1.toEntity()).withEntity(ref4.toEntity()).withEntity(ref5.toEntity())
                        .withEntity(ref6.toEntity()).withEntity(ref9.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        .withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        .withRelationships(e4.withRelations("hasEntityReference", ref9.id()))
                        .withRelationships(ev1.withRelations("hasEvalueReference", ref1.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref4.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref5.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testTwoSomeWithPropAndRel() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)), // e1,2,3
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.some, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev1.fieldId)), // ev1,3
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 10, 0), // 1,2,3
                        new EProp(10, "content", Constraint.of(ConstraintOp.eq, ref4.content))  // ev2,3 + ref2,4,5
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity()).withEntity(e3.toEntity())
                        .withEntity(ev1.toEntity()).withEntity(ev2.toEntity()).withEntity(ev3.toEntity())
                        .withEntity(ref2.toEntity()).withEntity(ref4.toEntity()).withEntity(ref5.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref4.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

}
