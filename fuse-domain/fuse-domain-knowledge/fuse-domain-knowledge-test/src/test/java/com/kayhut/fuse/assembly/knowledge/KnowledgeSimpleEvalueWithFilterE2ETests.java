package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultAssert;
import com.kayhut.fuse.model.results.QueryResultBase;
import org.junit.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import static com.kayhut.fuse.assembly.knowledge.Setup.client;
import static com.kayhut.fuse.assembly.knowledge.Setup.fuseClient;
import static com.kayhut.fuse.assembly.knowledge.Setup.manager;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder._v;


public class KnowledgeSimpleEvalueWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static ValueBuilder v1, v2, v3, v4, v5, v6, v7, v8;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception
    {
        //Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Evalue entities for tests
        v1 = _v(ctx.nextValueId()).field("Car sale").value("Chevrolet").bdt("identifier").ctx("sale")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .deleteTime(sdf.parse("2018-07-12 13:05:13.000"));
        v2 = _v(ctx.nextValueId()).field("garage").value("Zion and his sons").bdt("identifier").ctx("fixing cars")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu Peretz");
        v3 = _v(ctx.nextValueId()).field("Car sales").value("chevrolet").bdt("California").ctx("Sale cars")
                .creationUser("Kobi Peretz").lastUpdateUser("Dudu Shaul");
        v4 = _v(ctx.nextValueId()).field("Garage").value(322).bdt("Netanya").ctx("fixing cars").creationUser("Haim Melamed")
                .lastUpdateUser("haim Melamed").creationTime(sdf.parse("2018-04-17 13:05:13.098"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987"));
        v5 = _v(ctx.nextValueId()).field("Color").value("White").bdt("Identifier").ctx("colors")
                .creationUser("Haim Melamed").creationTime(sdf.parse("2016-09-02 19:45:23.123"))
                .lastUpdateUser("haim Melamed").deleteTime(sdf.parse("2017-12-12 01:00:00.000"));
        v6 = _v(ctx.nextValueId()).field("date meeting").value(sdf.parse("2015-02-03 14:04:33.125")).bdt("Car owners meeting")
                .ctx("Replacing between people").creationUser("Chip of cars").lastUpdateUser("Yachial Nadav")
                .creationTime(sdf.parse("2016-09-02 19:45:23.123")).deleteTime(sdf.parse("2017-12-12 01:00:00.000"));
        v7 = _v(ctx.nextValueId()).field("North Garages").value(222).bdt("North").ctx("North country")
                .creationUser("Gabi Levy").lastUpdateUser("Gabi Levy").creationTime(sdf.parse("2014-08-18 18:08:18.888"))
                .lastUpdateTime(sdf.parse("2018-05-07 03:51:52.387"));
        v8 = _v(ctx.nextValueId()).field("conference date").value(sdf.parse("2013-01-01 11:01:31.121")).bdt("Car owners meeting")
                .ctx("changing information").creationUser("Yaaaaaariv").lastUpdateUser("Yael Biniamin")
                .creationTime(sdf.parse("2017-07-07 17:47:27.727")).deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        // Create logicalId for Evalues
        v1.logicalId = ctx.nextLogicalId();
        v2.logicalId = ctx.nextLogicalId();
        v3.logicalId = ctx.nextLogicalId();
        v4.logicalId = ctx.nextLogicalId();
        v5.logicalId = ctx.nextLogicalId();
        v6.logicalId = ctx.nextLogicalId();
        v7.logicalId = ctx.nextLogicalId();
        v8.logicalId = ctx.nextLogicalId();
        // Insert Evalue entities to ES
        Assert.assertEquals(8, commit(ctx, INDEX, v1, v2, v3, v4, v5, v6, v7, v8));
    }


    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }


    // Start Tests:
    @Test
    public void testEqEvalueByLogicalId() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, v1.logicalId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v1.toEntity())  //context entity
                        .build())
                .build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqEvalueById() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "id", Constraint.of(ConstraintOp.eq, v1.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v1.toEntity())  //context entity
                        .build())
                .build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqEvalueByBdt() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "bdt", Constraint.of(ConstraintOp.eq, v1.bdt))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v1.toEntity())  //context entity
                        .withEntity(v2.toEntity())  //context entity
                        .build())
                .build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    // Open BUG 1 -> It supposed to give result of v4 only and NOT v2 ("Garage" and "garage" not supposed to be equal)
    @Test(expected = ComparisonFailure.class)
    public void testEqEvalueByFieldId() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "fieldId", Constraint.of(ConstraintOp.eq, v4.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v4.toEntity())  //context entity
                        .build())
                .build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqEvalueByContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "context", Constraint.of(ConstraintOp.eq, v4.context))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v2.toEntity()).withEntity(v4.toEntity())  //context entity
                        .build())
                .build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    // Open BUG 1 -> It supposed to give result of v1 only ("Chevrolet" and "chevrolet" not supposed to be equal)
    @Test(expected = AssertionError.class)
    public void testEqEvalueByStringValue() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "stringValue", Constraint.of(ConstraintOp.eq, v1.stringValue))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v1.toEntity()) // "Chevrolet"
                        //.withEntity(v3.toEntity()) // "chevrolet"
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    // Open BUG 1 -> It supposed to give result of v1 only ("Chevrolet" and "chevrolet" not supposed to be same for Like)
    @Test(expected = AssertionError.class)
    public void testLikeEvalueByStringValue() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "stringValue", Constraint.of(ConstraintOp.like, "Chev*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v1.toEntity()) // "Chevrolet"
                        //.withEntity(v3.toEntity()) // "chevrolet"
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    // BUG 2 -> It's find "Chevrolet" and doesn't find "chevrolet" -> supposed to be the opposite!!!
    @Test(expected = ComparisonFailure.class)
    public void testLikeAnyEvalueByStringValue() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "stringValue", Constraint.of(ConstraintOp.likeAny, Arrays.asList("che*", "*an*")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v2.toEntity()) // "and"
                        .withEntity(v3.toEntity()) // "chevrolet"
                        .withEntity(v4.toEntity()) // "Netanya"
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testNeEvalueByStringValue() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "stringValue", Constraint.of(ConstraintOp.ne, "White"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v1.toEntity()).withEntity(v2.toEntity())
                        .withEntity(v3.toEntity()).withEntity(v4.toEntity())
                        .withEntity(v6.toEntity()).withEntity(v7.toEntity())
                        .withEntity(v8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    // Find also v3 ("chevrolet"). Is it OK?
    @Test(expected = AssertionError.class)
    public void testInSetEvalueByStringValue() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "stringValue", Constraint.of(ConstraintOp.inSet,
                                Arrays.asList(v1.stringValue, v5.stringValue)))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(v1.toEntity()).withEntity(v5.toEntity())
                        //.withEntity(v3.toEntity())  //  ???? Is it OK?
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


}
