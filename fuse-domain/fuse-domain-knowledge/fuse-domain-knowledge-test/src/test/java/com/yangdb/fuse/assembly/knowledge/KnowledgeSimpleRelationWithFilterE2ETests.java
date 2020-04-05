package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
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
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;


public class KnowledgeSimpleRelationWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static RelationBuilder rel1, rel2, rel3, rel4, rel5, rel6, rel7, rel8;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        //Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Relation entities for tests
        rel1 = _rel(ctx.nextRelId()).ctx("Car companies").cat("Cars").creationUser("Liat Plesner")
                .lastUpdateUser("Yael Pery").creationTime(sdf.parse("2010-04-31 11:04:29.089"))
                .lastUpdateTime(sdf.parse("2018-01-01 00:39:56.000")).deleteTime(sdf.parse("2018-02-02 22:22:22.222"));
        rel2 = _rel(ctx.nextRelId()).ctx("Car Companies").cat("cars").creationUser("liat plesner")
                .lastUpdateUser("Yael pery").creationTime(sdf.parse("1990-00-00 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2018-01-01 00:39:56.000")).deleteTime(sdf.parse("2018-05-03 19:19:19.192"));
        rel3 = _rel(ctx.nextRelId()).ctx("Number of wheels").cat("Wheels").creationUser("Liat Moshe")
                .lastUpdateUser("yael pery").creationTime(sdf.parse("2010-04-31 11:04:29.089"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:41.489")).deleteTime(sdf.parse("2010-09-09 19:19:11.999"));
        rel4 = _rel(ctx.nextRelId()).ctx("Quantity of wheels").cat("wheels").creationUser("Yaacov Gabuy")
                .lastUpdateUser("Meir Pery").creationTime(sdf.parse("1999-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:42.489")).deleteTime(sdf.parse("2008-08-08 88:88:88.888"));
        rel5 = _rel(ctx.nextRelId()).ctx("Quantity of Wheels").cat("Wheels").creationUser("Yaacov")
                .lastUpdateUser("Moshe").creationTime(sdf.parse("2009-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2006-06-07 05:45:55.565")).deleteTime(sdf.parse("2004-02-03 11:11:11.022"));
        rel6 = _rel(ctx.nextRelId()).ctx("spare tire").cat("alternate wheel").creationUser("Moshe David Levi")
                .lastUpdateUser("Haim Ben Aharon").creationTime(sdf.parse("2014-12-01 12:24:36.786"))
                .lastUpdateTime(sdf.parse("2006-06-07 05:45:55.565")).deleteTime(sdf.parse("2002-02-03 11:11:11.022"));
        rel7 = _rel(ctx.nextRelId()).ctx("white car").cat("car").creationUser("Moshe Levi")
                .lastUpdateUser("Haim Ben Aharon").creationTime(sdf.parse("2017-02-29 02:41:41.489"))
                .lastUpdateTime(sdf.parse("2011-01-01 01:34:56.000")).deleteTime(sdf.parse("2001-02-03 11:11:11.022"));
        rel8 = _rel(ctx.nextRelId()).ctx("car sales").cat("Radio").creationUser("Moshe David Levi")
                .lastUpdateUser("Haim Aharon").creationTime(sdf.parse("2017-02-29 02:41:41.489"))
                .lastUpdateTime(sdf.parse("2011-01-01 01:34:56.000")).deleteTime(sdf.parse("2018-02-02 22:22:22.222"));
        // Insert Relation entities to ES
        Assert.assertEquals(8, commit(ctx, REL_INDEX, rel1, rel2, rel3, rel4, rel5, rel6, rel7, rel8));
    }

    @AfterClass

    public static void after() {
        if(ctx!=null) Assert.assertEquals(8,ctx.removeCreated());
}

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testEqRelationByContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "context", Constraint.of(ConstraintOp.eq, rel1.context))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity())  //context entity
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationById() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "id", Constraint.of(ConstraintOp.eq, rel6.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "category", Constraint.of(ConstraintOp.eq, rel3.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity()).withEntity(rel5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByCreationUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "creationUser", Constraint.of(ConstraintOp.eq, rel6.creationUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel6.toEntity()).withEntity(rel8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.eq, rel1.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity()).withEntity(rel3.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByLastUpdateUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "lastUpdateUser", Constraint.of(ConstraintOp.eq, rel2.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.eq, rel2.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity()).withEntity(rel2.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByDeleteTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "deleteTime", Constraint.of(ConstraintOp.eq, rel8.deleteTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity()).withEntity(rel8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByIdAndContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "context", Constraint.of(ConstraintOp.eq, rel5.context)),
                        new EProp(4, "id", Constraint.of(ConstraintOp.eq, rel5.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByCategoryAndContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "context", Constraint.of(ConstraintOp.eq, rel2.context)),
                        new EProp(4, "category", Constraint.of(ConstraintOp.eq, rel2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqRelationByCategoryContextAndDeleteTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "context", Constraint.of(ConstraintOp.eq, rel7.context)),
                        new EProp(3, "deleteTime", Constraint.of(ConstraintOp.eq, rel7.deleteTime)),
                        new EProp(4, "category", Constraint.of(ConstraintOp.eq, rel7.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testInSetRelationByCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "category", Constraint.of(ConstraintOp.inSet,
                                Arrays.asList(rel2.category, rel3.category)))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity()).withEntity(rel3.toEntity()).withEntity(rel5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeRelationStartOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "category", Constraint.of(ConstraintOp.like, "car*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity()).withEntity(rel7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeRelationEndOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "category", Constraint.of(ConstraintOp.like, "*heel"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeRelationContainsOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "category", Constraint.of(ConstraintOp.like, "*heel*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity()).withEntity(rel4.toEntity())
                        .withEntity(rel5.toEntity()).withEntity(rel6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeAnyRelationContainsOnCategory() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "category", Constraint.of(ConstraintOp.likeAny,  Arrays.asList("*ar*", "*ee*")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity()).withEntity(rel2.toEntity()).withEntity(rel3.toEntity())
                        .withEntity(rel4.toEntity()).withEntity(rel5.toEntity())
                        .withEntity(rel6.toEntity()).withEntity(rel7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testGtRelationByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.gt, rel3.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity()).withEntity(rel2.toEntity()).withEntity(rel4.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testGteRelationByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.ge, rel3.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity()).withEntity(rel2.toEntity())
                        .withEntity(rel3.toEntity()).withEntity(rel4.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLtRelationByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.lt, rel7.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel5.toEntity()).withEntity(rel6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLteRelationByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.le, rel7.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel5.toEntity()).withEntity(rel6.toEntity())
                        .withEntity(rel7.toEntity()).withEntity(rel8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testNeRelationByContext() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "context", Constraint.of(ConstraintOp.ne, rel1.context))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity()).withEntity(rel3.toEntity()).withEntity(rel4.toEntity())
                        .withEntity(rel5.toEntity()).withEntity(rel6.toEntity())
                        .withEntity(rel7.toEntity()).withEntity(rel8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEmptyRelationByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.eq, rel2.creationTime))
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
