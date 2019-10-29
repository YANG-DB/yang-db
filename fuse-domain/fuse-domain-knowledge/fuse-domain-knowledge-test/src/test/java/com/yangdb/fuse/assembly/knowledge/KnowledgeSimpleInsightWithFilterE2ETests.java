package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.InsightBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;
import static com.yangdb.fuse.assembly.knowledge.Setup.client;
import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.knowledge.Setup.manager;
import static com.yangdb.fuse.assembly.knowledge.domain.InsightBuilder.INSIGHT_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.InsightBuilder._i;


public class KnowledgeSimpleInsightWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static InsightBuilder i1, i2, i3, i4, i5, i6, i7, i8, i9;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        //Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
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
        i5 = _i(ctx.nextInsightId()).context("car treatments").content("jaki Garage")
                .entityIds(Arrays.asList("e00000001", "e00000007", "e00000008", "e00000009")).creationUser("Ayal Levi")
                .lastUpdateUser("Rony Levi").creationTime(sdf.parse("2016-09-22 02:51:53.463"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987")).deleteTime(sdf.parse("2018-07-12 09:01:03.764"));
        i6 = _i(ctx.nextInsightId()).context("car treatments").content("jaki Garage")
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
        // Insert Relation entities to ES
        Assert.assertEquals(9, commit(ctx, INSIGHT_INDEX, i1, i2, i3, i4, i5, i6, i7, i8, i9));
    }

    @AfterClass

    public static void after() {
        if(ctx!=null) Assert.assertEquals(9,ctx.removeCreated());
}

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper


    // Start Tests:
    @Test
    public void testEqInsightById() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "id", Constraint.of(ConstraintOp.eq, i1.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity())  //context entity
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.eq, i6.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i3.toEntity()).withEntity(i4.toEntity())
                        .withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "context", Constraint.of(ConstraintOp.eq, i5.context))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i3.toEntity()).withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByEntities() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "entityIds", Constraint.of(ConstraintOp.inSet, i1.entityIds))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i2.toEntity()).withEntity(i5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByCreationUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "creationUser", Constraint.of(ConstraintOp.eq, i4.creationUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i3.toEntity()).withEntity(i4.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.eq, i4.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i4.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByLastUpdateUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "lastUpdateUser", Constraint.of(ConstraintOp.eq, i5.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.eq, i7.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i7.toEntity()).withEntity(i8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByDeleteTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "deleteTime", Constraint.of(ConstraintOp.eq, i6.deleteTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i4.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightByIdAndContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, i9.content)),
                        new EProp(4, "id", Constraint.of(ConstraintOp.eq, i9.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i9.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightContextAndContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, i6.content)),
                        new EProp(4, "context", Constraint.of(ConstraintOp.eq, i6.context))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i3.toEntity()).withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqInsightContextContentAndDeleteTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, i6.content)),
                        new EProp(4, "context", Constraint.of(ConstraintOp.eq, i6.context)),
                        new EProp(5, "deleteTime", Constraint.of(ConstraintOp.eq, i6.deleteTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testInSetInsightByContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.inSet, Arrays.asList(i8.content, i9.content)))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i7.toEntity()).withEntity(i8.toEntity()).withEntity(i9.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeInsightStartOnContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.like, "Shlomi *"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i7.toEntity()).withEntity(i8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeInsightEndOnContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.like, "* companies"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i2.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeInsightContainsOnContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.like, "*ar*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i3.toEntity()).withEntity(i4.toEntity()).withEntity(i5.toEntity())
                        .withEntity(i6.toEntity()).withEntity(i7.toEntity())
                        .withEntity(i8.toEntity()).withEntity(i9.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeInsightContainsOnContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "context", Constraint.of(ConstraintOp.like, "*ar*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i2.toEntity()).withEntity(i3.toEntity())
                        .withEntity(i4.toEntity()).withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .withEntity(i7.toEntity()).withEntity(i8.toEntity()).withEntity(i9.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeAnyInsightContainsOnContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "context", Constraint.of(ConstraintOp.likeAny, Arrays.asList("*comp*", "*tme*")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i2.toEntity()).withEntity(i3.toEntity())
                        .withEntity(i4.toEntity()).withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeAnyInsightContainsOnContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.likeAny, Arrays.asList("*rof*", "*aki*")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i2.toEntity()).withEntity(i3.toEntity())
                        .withEntity(i4.toEntity()).withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testGtInsightByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.gt, i6.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i3.toEntity()).withEntity(i4.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testGteInsightByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.ge, i6.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i2.toEntity()).withEntity(i3.toEntity())
                        .withEntity(i4.toEntity()).withEntity(i5.toEntity()).withEntity(i6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLtInsightByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.lt, i9.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i7.toEntity()).withEntity(i8.toEntity())

                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLteInsightByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.le, i9.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i7.toEntity()).withEntity(i8.toEntity()).withEntity(i9.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testNeInsightByContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.ne, i4.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(i1.toEntity()).withEntity(i2.toEntity()).withEntity(i7.toEntity())
                        .withEntity(i8.toEntity()).withEntity(i9.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEmptyInsightByCreationUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Insight", 2, 0),
                        new EProp(2, "creationUser", Constraint.of(ConstraintOp.eq, "Haim Kanias"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

}
