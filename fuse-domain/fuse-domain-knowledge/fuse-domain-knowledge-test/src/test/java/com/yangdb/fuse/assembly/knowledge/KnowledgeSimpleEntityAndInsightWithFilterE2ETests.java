package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.InsightBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
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
import java.util.TimeZone;
import static com.yangdb.fuse.assembly.knowledge.Setup.client;
import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.knowledge.Setup.manager;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.InsightBuilder.INSIGHT_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.InsightBuilder._i;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.countEntitiesAndAdditionals;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


public class KnowledgeSimpleEntityAndInsightWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4;
    static InsightBuilder i1, i2, i3, i4, i5, i6, i7, i8, i9;
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
        // Insight entities for tests
        i1 = _i(ctx.nextInsightId()).context("cars companies").content("Profitable companies")
                .entityIds(Arrays.asList("e00000001", "e00000002")).creationUser("kobi Shaul")
                .lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987")).deleteTime(sdf.parse("2018-07-12 09:01:03.764"));
        i2 = _i(ctx.nextInsightId()).context("Cars companies").content("Very profitable companies")
                .entityIds(Arrays.asList("e00000001", "e00000002")).creationUser("kobi Shaul")
                .lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2016-09-22 02:51:53.463"))
                .lastUpdateTime(sdf.parse("2023-06-16 16:56:56.966")).deleteTime(sdf.parse("2030-12-27 07:07:07.767"));
        i3 = _i(ctx.nextInsightId()).context("car treatments").content("Jaki garage")
                .entityIds(Arrays.asList("e00000003", "e00000004", "e00000005")).creationUser("Ayal Shaul")
                .lastUpdateUser("Rami Levi").creationTime(sdf.parse("2025-01-01 01:11:01.711"))
                .lastUpdateTime(sdf.parse("2023-06-16 16:56:56.966")).deleteTime(sdf.parse("2030-12-27 07:07:07.767"));
        i4 = _i(ctx.nextInsightId()).context("car Treatments").content("jaki Garage")
                .entityIds(Arrays.asList("e00000005", "e00000006", "e00000007", "e00000008")).creationUser("Ayal Shaul")
                .lastUpdateUser("Rami Levi").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .lastUpdateTime(sdf.parse("2019-12-29 23:59:59.999")).deleteTime(sdf.parse("2029-03-13 03:31:33.333"));
        i5 = _i(ctx.nextInsightId()).context("car treatments").content("shlomi auto parts")
                .entityIds(Arrays.asList("e00000001", "e00000007", "e00000008", "e00000009")).creationUser("Ayal Levi")
                .lastUpdateUser("Rony Levi").creationTime(sdf.parse("2016-09-22 02:51:53.463"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987")).deleteTime(sdf.parse("2018-07-12 09:01:03.764"));
        i6 = _i(ctx.nextInsightId()).context("car treatments").content("Gabi Garage")
                .entityIds(Arrays.asList("e00000003", "e00000009", "e00000010", "e00000011")).creationUser("Ayal Levi")
                .lastUpdateUser("Rony Levi").creationTime(sdf.parse("2016-09-22 02:51:53.463"))
                .lastUpdateTime(sdf.parse("2023-06-16 16:56:56.966")).deleteTime(sdf.parse("2029-03-13 03:31:33.333"));
        i7 = _i(ctx.nextInsightId()).context("Selling Auto Parts").content("Shlomi Auto Parts")
                .entityIds(Arrays.asList("e00000004", "e00000006", "e00000008", "e00000011")).creationUser("Gabi Lamed")
                .lastUpdateUser("Dor Alon").creationTime(sdf.parse("1980-10-10 10:10:10.101"))
                .lastUpdateTime(sdf.parse("2017-08-18 18:58:58.868")).deleteTime(sdf.parse("2019-07-15 43:38:23.363"));
        i8 = _i(ctx.nextInsightId()).context("selling auto Parts").content("Shlomi auto parts")
                .entityIds(Arrays.asList("e00000011", "e00000012", "e00000013", "e00000014")).creationUser("gabi lamed")
                .lastUpdateUser("dor Alon").creationTime(sdf.parse("1969-12-30 13:30:30.131"))
                .lastUpdateTime(sdf.parse("2017-08-18 18:58:58.868")).deleteTime(sdf.parse("2019-07-15 43:38:23.363"));
        i9 = _i(ctx.nextInsightId()).context("volvo white cars").content("white family cars")
                .entityIds(Arrays.asList("e00000007", "e00000009", "e00000013", "e00000015")).creationUser("gabi lamed")
                .lastUpdateUser("dor Alon").creationTime(sdf.parse("2004-04-22 12:22:20.232"))
                .lastUpdateTime(sdf.parse("2015-05-15 15:25:35.558")).deleteTime(sdf.parse("2022-04-14 43:34:43.343"));
        // Add Insight to Entity
        e1.insight(i1);
        e1.insight(i2);
        e2.insight(i3);
        e2.insight(i4);
        e2.insight(i5);
        e3.insight(i6);
        e4.insight(i7);
        e4.insight(i8);
        e4.insight(i9);
        // Insert Entity and Insight entities to ES
        Assert.assertEquals(countEntitiesAndAdditionals(e1, e2, e3, e4), commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals(9, commit(ctx, INSIGHT_INDEX, i1, i2, i3, i4, i5, i6, i7, i8, i9));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testEqByEntityCategoryAndInsightContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e2.category)),
                        new Rel(4, "hasInsight", R, null, 5, 0),
                        new ETyped(5, "I", "Insight", 6, 0),
                        new EProp(6, "content", Constraint.of(ConstraintOp.eq, i3.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(i3.toEntity())
                        .withEntity(i4.toEntity())
                        .withRelationships(e2.withRelations("hasInsight", i3.id()))
                        .withRelationships(e2.withRelations("hasInsight", i4.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class) // If I add condition on entity it will work (see test below: testEqByInsightContentAndEntityLastUpdateUser)
    public void testEqByInsightContentAndEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, i7.content)),
                        new Rel(4, "hasInsight", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(e4.toEntity())
                        .withEntity(i5.toEntity())
                        .withEntity(i7.toEntity())
                        .withEntity(i8.toEntity())
                        .withRelationships(e2.withRelations("hasInsight", i5.id()))
                        .withRelationships(e4.withRelations("hasInsight", i7.id()))
                        .withRelationships(e4.withRelations("hasInsight", i8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndInsight() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e4.category)),
                        new Rel(4, "hasInsight", R, null, 5, 0),
                        new ETyped(5, "I", "Insight", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(i7.toEntity())
                        .withEntity(i8.toEntity())
                        .withEntity(i9.toEntity())
                        .withRelationships(e4.withRelations("hasInsight", i7.id()))
                        .withRelationships(e4.withRelations("hasInsight", i8.id()))
                        .withRelationships(e4.withRelations("hasInsight", i9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByInsightContentAndEntityLastUpdateUser() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, i8.content)),
                        new Rel(4, "hasInsight", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e4.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(i7.toEntity())
                        .withEntity(i8.toEntity())
                        .withRelationships(e4.withRelations("hasInsight", i7.id()))
                        .withRelationships(e4.withRelations("hasInsight", i8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByStrongEntityCategoryAndWeakInsightContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e4.category)),
                        new Rel(4, "hasInsight", R, null, 5, 0),
                        new ETyped(5, "I", "Insight", 6, 0),
                        new EProp(6, "content", Constraint.of(ConstraintOp.eq, i8.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(i7.toEntity())
                        .withEntity(i8.toEntity())
                        .withRelationships(e4.withRelations("hasInsight", i7.id()))
                        .withRelationships(e4.withRelations("hasInsight", i8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndInsightContainsContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e2.category)),
                        new Rel(4, "hasInsight", R, null, 5, 0),
                        new ETyped(5, "I", "Insight", 6, 0),
                        new EProp(6, "content", Constraint.of(ConstraintOp.like, "*ar*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(i3.toEntity())
                        .withEntity(i4.toEntity())
                        .withEntity(i5.toEntity())
                        .withRelationships(e2.withRelations("hasInsight", i3.id()))
                        .withRelationships(e2.withRelations("hasInsight", i4.id()))
                        .withRelationships(e2.withRelations("hasInsight", i5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Ignore
    @Test
    public void testEqByInsightContentAndEntityContainsCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, i7.content)),
                        new Rel(4, "hasInsight", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.like, "*pe*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(i5.toEntity())
                        .withRelationships(e2.withRelations("hasInsight", i5.id()))

                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalInsightContent_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "ope*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", R, null, 6, 0),
                        new ETyped(6, "I", "Insight", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, i3.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(i3.toEntity())
                        .withEntity(i4.toEntity())
                        .withRelationships(e2.withRelations("hasInsight", i3.id()))
                        .withRelationships(e2.withRelations("hasInsight", i4.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalInsightContent_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*itro*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", R, null, 6, 0),
                        new ETyped(6, "I", "Insight", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, i6.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(i6.toEntity())
                        .withRelationships(e3.withRelations("hasInsight", i6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalInsightContent_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*azd*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", R, null, 6, 0),
                        new ETyped(6, "I", "Insight", 7, 0),
                        new EProp(7, "content", Constraint.of(ConstraintOp.eq, i6.content))
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
    public void testLikeByEntityCategoryAndOptionalInsight() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*maz*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", R, null, 6, 0),
                        new ETyped(6, "I", "Insight", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(i7.toEntity())
                        .withEntity(i8.toEntity())
                        .withEntity(i9.toEntity())
                        .withRelationships(e4.withRelations("hasInsight", i7.id()))
                        .withRelationships(e4.withRelations("hasInsight", i8.id()))
                        .withRelationships(e4.withRelations("hasInsight", i9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class) // BUG: pageData doesn't contain e2
    public void testLikeByInsightContentAndOptionalEntityCategory_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*ar*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity()).withEntity(i3.toEntity())
                        .withEntity(i4.toEntity()).withEntity(i5.toEntity())
                        .withEntity(i6.toEntity()).withEntity(i7.toEntity())
                        .withEntity(i8.toEntity()).withEntity(i9.toEntity())
                        .withRelationships(e2.withRelations("hasInsight", i3.id()))
                        .withRelationships(e2.withRelations("hasInsight", i4.id()))
                        .withRelationships(e2.withRelations("hasInsight", i5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByInsightContentAndOptionalEntityCategory_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*family*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i9.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class) // BUG: pageData doesn't contain e3
    public void testLikeByInsightContentAndOptionalEntityCategory_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*abi*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e3.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(i6.toEntity())
                        .withRelationships(e3.withRelations("hasInsight", i6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class)  // BUG: pageData doesn't contain e1, e2 and e3
    public void testLikeByInsightContentAndOptionalEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*ar*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasInsight", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity()).withEntity(e3.toEntity())
                        .withEntity(e4.toEntity()).withEntity(i3.toEntity())
                        .withEntity(i4.toEntity()).withEntity(i5.toEntity())
                        .withEntity(i6.toEntity()).withEntity(i7.toEntity())
                        .withEntity(i8.toEntity()).withEntity(i9.toEntity())
                        .withRelationships(e2.withRelations("hasInsight", i3.id()))
                        .withRelationships(e2.withRelations("hasInsight", i4.id()))
                        .withRelationships(e2.withRelations("hasInsight", i5.id()))
                        .withRelationships(e3.withRelations("hasInsight", i6.id()))
                        .withRelationships(e4.withRelations("hasInsight", i7.id()))
                        .withRelationships(e4.withRelations("hasInsight", i8.id()))
                        .withRelationships(e4.withRelations("hasInsight", i9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalInsightContent() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*pel*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e1.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasInsight", R, null, 7, 0),
                        new ETyped(7, "I", "Insight", 8, 0),
                        new EProp(8, "content", Constraint.of(ConstraintOp.eq, i1.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(i1.toEntity())
                        .withRelationships(e1.withRelations("hasInsight", i1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalInsightContent_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*ope*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e2.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasInsight", R, null, 7, 0),
                        new ETyped(7, "I", "Insight", 8, 0),
                        new EProp(8, "content", Constraint.of(ConstraintOp.eq, i9.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByInsightContentAndEntity_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "I", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.like, "*notExist*")),
                        new Rel(4, "hasInsight", L, null, 5, 0),
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
