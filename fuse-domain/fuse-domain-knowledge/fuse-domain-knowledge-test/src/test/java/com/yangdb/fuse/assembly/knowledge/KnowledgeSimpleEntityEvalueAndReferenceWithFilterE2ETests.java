package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.*;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultAssert;
import com.yangdb.fuse.model.results.QueryResultBase;
import org.junit.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import static com.yangdb.fuse.assembly.knowledge.Setup.client;
import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.knowledge.Setup.manager;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder._ref;
import static com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


//@Ignore // TODO: fix BUG of adding logicalId to reference query results
public class KnowledgeSimpleEntityEvalueAndReferenceWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static ValueBuilder ev1, ev2, ev3, ev4, ev5;
    static EntityBuilder e1, e2, e3, e4;
    static RefBuilder ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
        //Setup.setup();
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
        ref5 = _ref(ctx.nextRefId()).title("engine").content("Quantity of different gears parts").url("https://www.carsforum.co.il")
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
        // Add Relation between Entities, Evalues and References
        e1.value(ev1.reference(ref1));
        e2.value(ev2.reference(ref2).reference(ref3));
        e3.value(ev3.reference(ref4).reference(ref5).reference(ref6));
        e4.value(ev4.reference(ref7), ev5.reference(ref8));
        // Insert Relation and Rvalue entities to ES
        Assert.assertEquals(4, commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals(5, commit(ctx, INDEX, ev1, ev2, ev3, ev4, ev5));
        Assert.assertEquals(8, commit(ctx, REF_INDEX, ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8));
    }


    @AfterClass

    public static void after() {
        if(ctx!=null) Assert.assertEquals(17,ctx.removeCreated());
}

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper


    // Start Tests:
    @Test
    public void testEqByEntityCategoryAndEvalueFieldIdAndReferenceContent_Chain() throws IOException, InterruptedException {
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
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev1.fieldId)), // 1,3
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 10, 0),
                        new EProp(10, "content", Constraint.of(ConstraintOp.eq, ref1.content)) // 1,3,6
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(ev1.toEntity())
                        .withEntity(ref1.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        .withRelationships(ev1.withRelations("hasEvalueReference", ref1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndEvalueFieldIdAndReferenceContent_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, ev1.fieldId)),
                        new Rel(4, "hasEvalueReference", R, null, 6, 0),
                        new Rel(5, "hasEvalue", L, null, 8, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, ref1.content)),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.eq, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(ev1.toEntity())
                        .withEntity(ref1.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        .withRelationships(ev1.withRelations("hasEvalueReference", ref1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndEvalueFieldIdAndReference_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e2.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev2.fieldId)),
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 10, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref2.toEntity()).withEntity(ref3.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndEvalueFieldIdAndReference_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, ev2.fieldId)),
                        new Rel(4, "hasEvalueReference", R, null, 6, 0),
                        new Rel(5, "hasEvalue", L, null, 7, 0),
                        new ETyped(6, "R", "Reference", 9, 0),
                        new ETyped(7, "A", "Entity", 8, 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref2.toEntity()).withEntity(ref3.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityCategoryAndEvalueAndReferenceContent_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e3.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.all, Collections.singletonList(7) , 0),
                        new Rel(7, "hasEvalueReference", R, null, 8, 0),
                        new ETyped(8, "R", "Reference", 9, 0),
                        new EProp(9, "content", Constraint.of(ConstraintOp.eq, ref6.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(ev3.toEntity())
                        .withEntity(ref6.toEntity())
                        .withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndEvalueAndReferenceContent_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new Rel(3, "hasEvalueReference", R, null, 5, 0),
                        new Rel(4, "hasEvalue", L, null, 7, 0),
                        new ETyped(5, "R", "Reference", 6, 0),
                        new EProp(6, "content", Constraint.of(ConstraintOp.eq, ref6.content)),
                        new ETyped(7, "A", "Entity", 8, 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, e3.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(ev3.toEntity())
                        .withEntity(ref6.toEntity())
                        .withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityAndEvalueFieldIdAndReferenceLastUpdateUser_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0), // 2,4
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "R1", "Evalue", 4, 0),
                        new Quant1(4, QuantType.all, Arrays.asList(5, 6), 0),
                        new EProp(5, "fieldId", Constraint.of(ConstraintOp.eq, ev5.fieldId)), // 2,5
                        new Rel(6, "hasEvalueReference", R, null, 7, 0),
                        new ETyped(7, "R", "Reference", 8, 0),
                        new EProp(8, "lastUpdateUser", Constraint.of(ConstraintOp.eq, ref8.lastUpdateUser)) // 2,8
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ev5.toEntity())
                        .withEntity(ref8.toEntity())
                        .withRelationships(e4.withRelations("hasEvalue", ev5.id()))
                        .withRelationships(ev5.withRelations("hasEvalueReference", ref8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityAndEvalueFieldIdAndReferenceLastUpdateUser_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, ev5.fieldId)),
                        new Rel(4, "hasEvalueReference", R, null, 6, 0),
                        new Rel(5, "hasEvalue", L, null, 8, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "lastUpdateUser", Constraint.of(ConstraintOp.eq, ref8.lastUpdateUser)),
                        new ETyped(8, "A", "Entity", 9, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ev5.toEntity())
                        .withEntity(ref8.toEntity())
                        .withRelationships(e4.withRelations("hasEvalue", ev5.id()))
                        .withRelationships(ev5.withRelations("hasEvalueReference", ref8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityAndEvalueFieldIdAndReferenceContent_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "R1", "Evalue", 4, 0),
                        new Quant1(4, QuantType.all, Arrays.asList(5, 6), 0),
                        new EProp(5, "fieldId", Constraint.of(ConstraintOp.eq, ev2.fieldId)),
                        new Rel(6, "hasEvalueReference", R, null, 7, 0),
                        new ETyped(7, "R", "Reference", 8, 0),
                        new EProp(8, "content", Constraint.of(ConstraintOp.eq, ref2.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref2.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityAndEvalueFieldIdAndReferenceContent_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, ev2.fieldId)),
                        new Rel(4, "hasEvalueReference", R, null, 6, 0),
                        new Rel(5, "hasEvalue", L, null, 8, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, ref2.content)),
                        new ETyped(8, "A", "Entity", 9, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref2.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityCategoryAndEvalueContainsFieldIdAndReferenceContent_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.like, "*ara*")),
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 10, 0),
                        new EProp(10, "content", Constraint.of(ConstraintOp.eq, ref2.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref2.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndEvalueContainsFieldIdAndReferenceContent_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*ara*")),
                        new Rel(4, "hasEvalueReference", R, null, 6, 0),
                        new Rel(5, "hasEvalue", L, null, 8, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, ref2.content)),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.eq, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref2.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityCategoryAndEvalueFieldIdAndReferenceContainsContent_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e4.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev4.fieldId)),
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 10, 0),
                        new EProp(10, "content", Constraint.of(ConstraintOp.like, "*ize*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ev4.toEntity())
                        .withEntity(ref7.toEntity())
                        .withRelationships(e4.withRelations("hasEvalue", ev4.id()))
                        .withRelationships(ev4.withRelations("hasEvalueReference", ref7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndEvalueFieldIdAndReferenceContainsContent_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, ev4.fieldId)),
                        new Rel(4, "hasEvalueReference", R, null, 6, 0),
                        new Rel(5, "hasEvalue", L, null, 8, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.like, "*ize*")),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.eq, e4.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ev4.toEntity())
                        .withEntity(ref7.toEntity())
                        .withRelationships(e4.withRelations("hasEvalue", ev4.id()))
                        .withRelationships(ev4.withRelations("hasEvalueReference", ref7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityContainsCategoryAndEvalueFieldIdAndReferenceContent_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*pel")),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "R1", "Evalue", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, ev2.fieldId)),
                        new Rel(8, "hasEvalueReference", R, null, 9, 0),
                        new ETyped(9, "R", "Reference", 10, 0),
                        new EProp(10, "content", Constraint.of(ConstraintOp.eq, ref3.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref3.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void tesLikeByEntityContainsCategoryAndEvalueFieldIdAndReferenceContent_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, ev2.fieldId)),
                        new Rel(4, "hasEvalueReference", R, null, 6, 0),
                        new Rel(5, "hasEvalue", L, null, 8, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, ref3.content)),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.like, "*pel"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref3.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByOptionalEntityCategoryAndOptionalEvalueFieldIdAndReferenceContainsContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "Quantity*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEvalueReference", L, null, 6, 0),
                        new ETyped(6, "R1", "Evalue", 7, 0),
                        new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                        new EProp(8, "fieldId", Constraint.of(ConstraintOp.eq, ev3.fieldId)),
                        new OptionalComp(9, 10),
                        new Rel(10, "hasEvalue", L, null, 11, 0),
                        new ETyped(11, "A", "Entity", 12, 0),
                        new EProp(12, "category", Constraint.of(ConstraintOp.like, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ev3.toEntity())
                        .withEntity(ref2.toEntity())
                        .withEntity(ref4.toEntity())
                        .withEntity(ref5.toEntity())
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref4.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityContainsCategoryAndOptionalEvalueAndReferenceContainsContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*Quantity*")), // 2,4,5
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEvalueReference", L, null, 6, 0), //2,3
                        new ETyped(6, "R1", "Evalue", 7, 0),
                        new Rel(7, "hasEvalue", L, null, 8, 0),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.like, "*itro*")) // 3
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(ev3.toEntity())
                        .withEntity(ref2.toEntity())
                        .withEntity(ref4.toEntity())
                        .withEntity(ref5.toEntity())
                        .withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref4.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void tesLikeByEntityContainsCategoryAndEvalueContainsFieldIdAndOptionalReferenceContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 6), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*sale")),  // 1,3
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEvalueReference", R, null, 7, 0),
                        new Rel(6, "hasEvalue", L, null, 9, 0),
                        new ETyped(7, "R", "Reference", 8, 0),
                        new EProp(8, "content", Constraint.of(ConstraintOp.eq, ref1.content)), // 1,6
                        new ETyped(9, "A", "Entity", 10, 0),
                        new EProp(10, "category", Constraint.of(ConstraintOp.like, "*e*")) // 1,3
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e3.toEntity())
                        .withEntity(ev1.toEntity()).withEntity(ev3.toEntity())
                        .withEntity(ref1.toEntity()).withEntity(ref6.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        .withRelationships(e3.withRelations("hasEvalue", ev3.id()))
                        .withRelationships(ev1.withRelations("hasEvalueReference", ref1.id()))
                        .withRelationships(ev3.withRelations("hasEvalueReference", ref6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void tesLikeByOptionalEntityAndEvalueContainsFieldIdAndOptionalReference() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Evalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 6), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*ara*")),  // 2,4,5
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEvalueReference", R, null, 8, 0),
                        new OptionalComp(6, 7),
                        new Rel(7, "hasEvalue", L, null, 9, 0),
                        new ETyped(8, "R", "Reference", 11, 0),  // 2,3,7,8
                        new ETyped(9, "A", "Entity", 10, 0) // 2,4
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity()).withEntity(e4.toEntity())
                        .withEntity(ev2.toEntity()).withEntity(ev4.toEntity()).withEntity(ev5.toEntity())
                        .withEntity(ref2.toEntity()).withEntity(ref3.toEntity())
                        .withEntity(ref7.toEntity()).withEntity(ref8.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(e4.withRelations("hasEvalue", ev4.id()))
                        .withRelationships(e4.withRelations("hasEvalue", ev5.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref3.id()))
                        .withRelationships(ev4.withRelations("hasEvalueReference", ref7.id()))
                        .withRelationships(ev5.withRelations("hasEvalueReference", ref8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityContainsCategoryAndOptionalEvalueFieldIdAndReferenceContainsContentLastUpdateUser() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(

                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*ize*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.like, "*argo*")), // 1,3,//7,8
                        new OptionalComp(5, 6),
                        new Rel(6, "hasEvalueReference", L, null, 7, 0),
                        new ETyped(7, "R1", "Evalue", 8, 0),
                        new Quant1(8, QuantType.all, Arrays.asList(9, 10), 0),
                        new EProp(9, "fieldId", Constraint.of(ConstraintOp.eq, ev4.fieldId)), // 2,//4
                        new Rel(10, "hasEvalue", L, null, 11, 0),
                        new ETyped(11, "A", "Entity", 12, 0),
                        new EProp(12, "category", Constraint.of(ConstraintOp.like, "*pel*"))  // 2,//1
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ev2.toEntity())
                        .withEntity(ref1.toEntity()).withEntity(ref3.toEntity())
                        .withEntity(ref7.toEntity()).withEntity(ref8.toEntity())
                        .withRelationships(e2.withRelations("hasEvalue", ev2.id()))
                        .withRelationships(ev2.withRelations("hasEvalueReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityCategoryAndOptionalEvalueFieldIdAndReference() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)), // 1,2
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEvalue", R, null, 6, 0),
                        new ETyped(6, "R1", "Evalue", 7, 0),
                        new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                        new EProp(8, "fieldId", Constraint.of(ConstraintOp.eq, ev1.fieldId)), // 1
                        new Rel(9, "hasEvalueReference", R, null, 10, 0),
                        new ETyped(10, "R", "Reference", 11, 0) // 1,2,3
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(ev1.toEntity())
                        .withEntity(ref1.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", ev1.id()))
                        .withRelationships(ev1.withRelations("hasEvalueReference", ref1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

}
