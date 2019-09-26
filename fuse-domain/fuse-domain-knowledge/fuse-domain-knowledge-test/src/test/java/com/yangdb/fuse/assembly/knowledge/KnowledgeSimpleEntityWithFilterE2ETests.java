package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.*;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.*;
import org.junit.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.Filter.filter;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KnowledgeQueryBuilder.start;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;


public class KnowledgeSimpleEntityWithFilterE2ETests {
    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception
    {
//        Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Entities for tests
        e1 = _e(ctx.nextLogicalId()).cat("opel").ctx("context1").creationTime(sdf.parse("2018-01-28 14:33:53.567"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e2 = _e(ctx.nextLogicalId()).cat("mazda").ctx("context2").lastUpdateTime(sdf.parse("2017-03-20 12:12:35.111"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e3 = _e(ctx.nextLogicalId()).cat("opel").ctx("context3").lastUpdateUser("Kobi Shaul")
                .deleteTime(sdf.parse("2018-02-09 02:02:02.222"));
        e4 = _e(e3.logicalId).cat("reno").ctx("context1").creationUser("Dudi Frid")
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e5 = _e(ctx.nextLogicalId()).cat("mitsubishi").ctx("context5").lastUpdateUser("Dudi Frid")
                .creationUser("Kobi Shaul").creationTime(sdf.parse("2018-02-28 23:55:13.899"))
                .lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"));
        e6 = _e(ctx.nextLogicalId()).cat("lexus").ctx("context6").creationUser("Kobi").lastUpdateUser("Kobi");
        e7 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context6").creationUser("Haim");
        e8 = _e(ctx.nextLogicalId()).cat("lexus").ctx("context7").lastUpdateUser("Haim")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000"));
        e9 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context8").lastUpdateUser("Dudi")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"));
        e10 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context10").lastUpdateUser("Kobi").creationUser("Kobi Shaul")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000"));
        // Insert entities to ES
        Assert.assertEquals("error loading data ",10, commit(ctx, INDEX, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));
    }


    @AfterClass
    public static void after() {
        if(ctx!=null) Assert.assertEquals(10,ctx.removeCreated());
   }


    // Start Tests:
    @Test
    public void testEqById() throws IOException, InterruptedException
    { // The test checking 2 Ids - for e1 and e5
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Create query for e1.id
        Query query = start().withEntity(e1.getETag(), filter().and("id", Constraint.of(ConstraintOp.eq, e1.id()))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        // Create query for e5.id
        Query query2 = start().withEntity(e5.getETag(), filter().and("id", Constraint.of(ConstraintOp.eq, e5.id()))).build();
        QueryResultBase pageData2 = query(fuseClient, fuseResourceInfo, query2);

        // Create expectedResult - for e1
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .build()).build();
        // Create expectedResult - for e5
        AssignmentsQueryResult expectedResult2 = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);  // e1
        QueryResultAssert.assertEquals(expectedResult2, (AssignmentsQueryResult) pageData2, false, true);  // e5
    }

    @Test
    public void testEqByLogicalId() throws IOException, InterruptedException
    { // The test checking 2 different logicalIds - for e3 amd e9
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Create query for e3.logicalId
        Query query = start().withEntity(e3.getETag(), filter().and("logicalId", Constraint.of(ConstraintOp.eq, e3.logicalId))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        // Create query for e9.logicalId
        Query query2 = start().withEntity(e9.getETag(), filter().and("logicalId", Constraint.of(ConstraintOp.eq, e9.logicalId))).build();
        QueryResultBase pageData2 = query(fuseClient, fuseResourceInfo, query2);

        // Create expectedResult - for e3
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity()).withEntity(e4.toEntity())
                        .build()).build();
        // Create expectedResult - for e9
        AssignmentsQueryResult expectedResult2 = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e9.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);  // e3
        QueryResultAssert.assertEquals(expectedResult2, (AssignmentsQueryResult) pageData2, false, true);  // e9
    }

    @Test
    public void testEqByCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e10.getETag(), filter().and("category", Constraint.of(ConstraintOp.eq, e10.category))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e7.toEntity())
                        .withEntity(e9.toEntity())
                        .withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e6.getETag(), filter().and("context", Constraint.of(ConstraintOp.eq, e6.context))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e7.toEntity())
                        .withEntity(e6.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByCreationUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e5.getETag(), filter().and("creationUser", Constraint.of(ConstraintOp.eq, e5.creationUser))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity())
                        .withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e8.getETag(), filter().and("creationTime", Constraint.of(ConstraintOp.eq, e8.creationTime))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e8.toEntity())
                        .withEntity(e9.toEntity())
                        .withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByLastUpdateUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e6.getETag(), filter().and("lastUpdateUser",
                Constraint.of(ConstraintOp.eq, e6.lastUpdateUser))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e6.toEntity())
                        .withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e5.getETag(), filter().and("lastUpdateTime",
                Constraint.of(ConstraintOp.eq, e5.lastUpdateTime))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity())
                        .withEntity(e9.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByDeleteTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag(), filter().and("deleteTime",
                Constraint.of(ConstraintOp.eq, e1.deleteTime))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(e4.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByLogicalIdAndCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag(), filter().and("category", Constraint.of(ConstraintOp.eq, e1.category))
                .and("logicalId", Constraint.of(ConstraintOp.eq, e1.logicalId))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByLogicalIdContentAndCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e6.getETag(), filter().and("category", Constraint.of(ConstraintOp.eq, e6.category))
                .and("logicalId", Constraint.of(ConstraintOp.eq, e6.logicalId))
                .and("context", Constraint.of(ConstraintOp.eq, e6.context))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e6.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEqByLogicalIdContentCategoryAndCreationUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e7.getETag(), filter().and("category", Constraint.of(ConstraintOp.eq, e7.category))
                .and("logicalId", Constraint.of(ConstraintOp.eq, e7.logicalId))
                .and("context", Constraint.of(ConstraintOp.eq, e7.context))
                .and("creationUser", Constraint.of(ConstraintOp.eq, e7.creationUser))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e7.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testInSetByLogicalId() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e9.getETag(), filter().and("logicalId",
                Constraint.of(ConstraintOp.inSet, Arrays.asList(e9.logicalId, e1.logicalId, e7.logicalId)))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e7.toEntity())
                        .withEntity(e9.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testLikeStartOnContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag(), filter().and("context", Constraint.of(ConstraintOp.like, "context1*"))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e4.toEntity())
                        .withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testLikeEndOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e2.getETag(), filter().and("category", Constraint.of(ConstraintOp.like, "*zda"))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testLikeMiddleOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag(), filter().and("category", Constraint.of(ConstraintOp.like, "*o*"))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e3.toEntity())
                        .withEntity(e4.toEntity()).withEntity(e7.toEntity())
                        .withEntity(e9.toEntity()).withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testLikeAnyOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag(), filter().and("category",
                Constraint.of(ConstraintOp.likeAny, Arrays.asList("*pe*", "*no*")))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()) // opel
                        .withEntity(e3.toEntity()) // opel
                        .withEntity(e4.toEntity()) // reno
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testGtOnCreationTime() throws IOException, InterruptedException, ParseException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e5.getETag(), filter().and("creationTime",
                Constraint.of(ConstraintOp.gt, sdf.parse("2018-05-12 13:05:13.000")))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity()).withEntity(e3.toEntity())
                        .withEntity(e4.toEntity()).withEntity(e6.toEntity())
                        .withEntity(e7.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testGteOnCreationTime() throws IOException, InterruptedException, ParseException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e5.getETag(), filter().and("creationTime",
                Constraint.of(ConstraintOp.ge, sdf.parse("2018-05-12 13:05:13.000")))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity()).withEntity(e3.toEntity())
                        .withEntity(e4.toEntity()).withEntity(e6.toEntity())
                        .withEntity(e7.toEntity()).withEntity(e8.toEntity())
                        .withEntity(e9.toEntity()).withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testLtOnCreationTime() throws IOException, InterruptedException, ParseException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag(), filter().and("creationTime",
                Constraint.of(ConstraintOp.lt, sdf.parse("2018-05-12 13:05:13.000")))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e5.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testLteOnCreationTime() throws IOException, InterruptedException, ParseException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag(), filter().and("creationTime",
                Constraint.of(ConstraintOp.le, sdf.parse("2018-05-12 13:05:13.000")))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e5.toEntity())
                        .withEntity(e8.toEntity()).withEntity(e9.toEntity())
                        .withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testNeByContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e6.getETag(), filter().and("context", Constraint.of(ConstraintOp.ne, e6.context))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(e3.toEntity()).withEntity(e4.toEntity())
                        .withEntity(e5.toEntity()).withEntity(e8.toEntity())
                        .withEntity(e9.toEntity()).withEntity(e10.toEntity())
                        .build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testEmptyLikeOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e2.getETag(), filter().and("category", Constraint.of(ConstraintOp.like, "*abc"))).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance().build()).build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

}
