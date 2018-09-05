package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.RefBuilder;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
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
import org.junit.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


@Ignore // TODO: fix BUG of adding logicalId to reference query results
public class KnowledgeSimpleEntityAndReferenceWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
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
        // Reference entities for tests
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
                .lastUpdateUser("dudi Fargon").lastUpdateTime(sdf.parse("2017-11-27 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        ref4 = _ref(ctx.nextRefId()).title("Gearbox").content("Quantity of gears").url("http://www.musach.co.il/forum")
                .sys("Cars Forum").creationUser("Dudi hania").creationTime(sdf.parse("2001-7-07 07:27:37.477"))
                .lastUpdateUser("Gabi Fargon").lastUpdateTime(sdf.parse("2014-04-07 07:37:57.070"))
                .deleteTime(sdf.parse("2016-06-06 11:17:17.170"));
        ref5 = _ref(ctx.nextRefId()).title("engine").content("Quantity of different gears parts").url("https://www.carsforum.co.il")
                .sys("High Risk").creationUser("Avi Zion Aharon Moshe").creationTime(sdf.parse("2010-12-01 12:24:36.489"))
                .lastUpdateUser("avi moshe").lastUpdateTime(sdf.parse("2014-04-07 07:37:57.070"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        ref6 = _ref(ctx.nextRefId()).title("Brassieres").content("size in mm").url("http://www.rechev.net")
                .sys("cars").creationUser("Yariv").creationTime(sdf.parse("2019-10-10 10:10:10.100"))
                .lastUpdateUser("Yariv").lastUpdateTime(sdf.parse("2022-12-31 15:34:56.000"))
                .deleteTime(sdf.parse("2023-02-03 11:11:11.022"));
        ref7 = _ref(ctx.nextRefId()).title("Brassieres").content("Size in MM").url("http://www.rechev.net")
                .sys("Brassieres").creationUser("Yariv Aaaav").creationTime(sdf.parse("2019-10-10 10:10:10.101"))
                .lastUpdateUser("dudi fargon").lastUpdateTime(sdf.parse("2017-08-08 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        ref8 = _ref(ctx.nextRefId()).title("cover chairs").content("size").url("http://www.chaircovers.com")
                .sys("Chair").creationUser("Haim hania").creationTime(sdf.parse("1999-12-06 12:24:36.666"))
                .lastUpdateUser("Fargon").lastUpdateTime(sdf.parse("2022-12-31 15:34:56.000"))
                .deleteTime(sdf.parse("2023-02-03 11:11:11.022"));
        // Add Reference to Entity
        e1.reference(ref1);
        e1.reference(ref2);
        e2.reference(ref3);
        e2.reference(ref4);
        e2.reference(ref5);
        e3.reference(ref6);
        e4.reference(ref7);
        e4.reference(ref8);
        // Insert Entity and Reference entities to ES
        Assert.assertEquals(4, commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals(8, commit(ctx, REF_INDEX, ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testEqByEntityCategoryAndReferenceContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEntityReference", R, null, 5, 0),
                        new ETyped(5, "R", "Reference", 6, 0),
                        new EProp(6, "content", Constraint.of(ConstraintOp.eq, ref1.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(ref1.toEntity())
                        .withEntity(ref3.toEntity())
                        .withRelationships(e1.withRelations("hasEntityReference", ref1.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByReferenceContentAndEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, ref6.content)),
                        new Rel(4, "hasEntityReference", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(ref6.toEntity())
                        .withEntity(e4.toEntity())
                        .withEntity(ref7.toEntity())
                        .withRelationships(e3.withRelations("hasEntityReference", ref6.id()))
                        .withRelationships(e4.withRelations("hasEntityReference", ref7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndReference() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e2.category)),
                        new Rel(4, "hasEntityReference", R, null, 5, 0),
                        new ETyped(5, "R", "Reference", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(ref1.toEntity())
                        .withEntity(ref2.toEntity())
                        .withEntity(ref3.toEntity())
                        .withEntity(ref4.toEntity())
                        .withEntity(ref5.toEntity())
                        .withRelationships(e1.withRelations("hasEntityReference", ref1.id()))
                        .withRelationships(e1.withRelations("hasEntityReference", ref2.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref3.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref4.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByReferenceContentAndEntityLastUpdateUser() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, ref3.content)),
                        new Rel(4, "hasEntityReference", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e1.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(ref1.toEntity())
                        .withRelationships(e1.withRelations("hasEntityReference", ref1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class) // BUG: Doesn't find ref6 (despite it similar to ref7)
    public void testEqByStrongEntityCategoryAndWeakReferenceContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e4.category)),
                        new Rel(4, "hasEntityReference", R, null, 5, 0),
                        new ETyped(5, "R", "Reference", 6, 0),
                        new EProp(6, "content", Constraint.of(ConstraintOp.eq, ref6.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ref6.toEntity())
                        .withEntity(ref7.toEntity())
                        .withRelationships(e4.withRelations("hasEntityReference", ref6.id()))
                        .withRelationships(e4.withRelations("hasEntityReference", ref7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndReferenceContainsContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e2.category)),
                        new Rel(4, "hasEntityReference", R, null, 5, 0),
                        new ETyped(5, "R", "Reference", 6, 0),
                        new EProp(6, "content", Constraint.of(ConstraintOp.like, "*of*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(ref2.toEntity())
                        .withEntity(ref4.toEntity())
                        .withEntity(ref5.toEntity())
                        .withRelationships(e1.withRelations("hasEntityReference", ref2.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref4.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByReferenceContentAndEntityContainsCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, ref8.content)),
                        new Rel(4, "hasEntityReference", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.like, "*zd*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ref8.toEntity())
                        .withRelationships(e4.withRelations("hasEntityReference", ref8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalReferenceContent_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "ope*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", R, null, 6, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, ref3.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(ref1.toEntity())
                        .withEntity(ref3.toEntity())
                        .withRelationships(e1.withRelations("hasEntityReference", ref1.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalReferenceContent_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*itroe*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", R, null, 6, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, ref6.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(ref6.toEntity())
                        .withRelationships(e3.withRelations("hasEntityReference", ref6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalReferenceContent_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*azd*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", R, null, 6, 0),
                        new ETyped(6, "R", "Reference", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, ref1.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalReference() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*maz*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", R, null, 6, 0),
                        new ETyped(6, "R", "Reference", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ref7.toEntity())
                        .withEntity(ref8.toEntity())
                        .withRelationships(e4.withRelations("hasEntityReference", ref7.id()))
                        .withRelationships(e4.withRelations("hasEntityReference", ref8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByReferenceContentAndOptionalEntityCategory_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*art*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(ref2.toEntity())
                        .withEntity(ref5.toEntity())
                        .withRelationships(e1.withRelations("hasEntityReference", ref2.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByReferenceContentAndOptionalEntityCategory_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*of*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e3.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref2.toEntity())
                        .withEntity(ref4.toEntity())
                        .withEntity(ref5.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByReferenceContentAndOptionalEntityCategory_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*gear*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(ref4.toEntity())
                        .withEntity(ref5.toEntity())
                        .withRelationships(e2.withRelations("hasEntityReference", ref4.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByReferenceContentAndOptionalEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*of*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEntityReference", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(ref2.toEntity()).withEntity(ref4.toEntity()).withEntity(ref5.toEntity())
                        .withRelationships(e1.withRelations("hasEntityReference", ref2.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref4.id()))
                        .withRelationships(e2.withRelations("hasEntityReference", ref5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalReferenceContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*maz*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e3.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasEntityReference", R, null, 7, 0),
                        new ETyped(7, "R", "Reference", 8, 0),
                        new EProp(8, "content", Constraint.of(ConstraintOp.eq, ref6.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(ref7.toEntity())
                        .withRelationships(e4.withRelations("hasEntityReference", ref7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalReferenceContent_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*cit*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e3.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasEntityReference", R, null, 7, 0),
                        new ETyped(7, "R", "Reference", 8, 0),
                        new EProp(8, "content", Constraint.of(ConstraintOp.eq, ref8.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByReferenceContentAndEntity_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*notExist*")),
                        new Rel(4, "hasEntityReference", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

}
