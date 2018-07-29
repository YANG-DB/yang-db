package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.RefBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.model.query.Query;
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
import java.util.TimeZone;
import static com.kayhut.fuse.assembly.knowledge.Setup.client;
import static com.kayhut.fuse.assembly.knowledge.Setup.fuseClient;
import static com.kayhut.fuse.assembly.knowledge.Setup.manager;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder._ref;


public class KnowledgeSimpleReferenceWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static RefBuilder r1, r2, r3, r4, r5, r6, r7, r8;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Reference entities for tests
        r1 = _ref(ctx.nextRefId()).title("steering wheel").content("size in cm").url("http://www.hadarrosen.com")
                .sys("HR").creationUser("Haim hania").creationTime(sdf.parse("2010-12-01 12:24:36.489"))
                .lastUpdateUser("dudi Fargon").lastUpdateTime(sdf.parse("2017-01-01 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        r2 = _ref(ctx.nextRefId()).title("Engine").content("Quantity of parts").url("https://www.carsforum.co.il")
                .sys("CARS FORUM").creationUser("Kobi Terminator David").creationTime(sdf.parse("2000-01-01 02:04:30.480"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2015-12-30 11:40:06.057"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.021"));
        r3 = _ref(ctx.nextRefId()).title("Steering Wheel").content("Size in CM").url("http://www.hadarrosen.com")
                .sys("Hr").creationUser("Haim Hania").creationTime(sdf.parse("2010-12-01 12:24:36.489"))
                .lastUpdateUser("dudi Fargon").lastUpdateTime(sdf.parse("2017-11-27 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        r4 = _ref(ctx.nextRefId()).title("Gearbox").content("Quantity of gears").url("http://www.musach.co.il/forum")
                .sys("Cars Forum").creationUser("Dudi hania").creationTime(sdf.parse("2001-7-07 07:27:37.477"))
                .lastUpdateUser("Gabi Fargon").lastUpdateTime(sdf.parse("2014-04-07 07:37:57.070"))
                .deleteTime(sdf.parse("2016-06-06 11:17:17.170"));
        r5 = _ref(ctx.nextRefId()).title("engine").content("Quantity of different parts").url("https://www.carsforum.co.il")
                .sys("High Risk").creationUser("Avi Zion Aharon Moshe").creationTime(sdf.parse("2010-12-01 12:24:36.489"))
                .lastUpdateUser("avi moshe").lastUpdateTime(sdf.parse("2014-04-07 07:37:57.070"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        r6 = _ref(ctx.nextRefId()).title("Brassieres").content("size in mm").url("http://www.rechev.net")
                .sys("cars").creationUser("Yariv").creationTime(sdf.parse("2019-10-10 10:10:10.100"))
                .lastUpdateUser("Yariv").lastUpdateTime(sdf.parse("2022-12-31 15:34:56.000"))
                .deleteTime(sdf.parse("2023-02-03 11:11:11.022"));
        r7 = _ref(ctx.nextRefId()).title("Brassieres").content("Size in MM").url("http://www.rechev.net")
                .sys("Brassieres").creationUser("Yariv Aaaav").creationTime(sdf.parse("2019-10-10 10:10:10.100"))
                .lastUpdateUser("dudi fargon").lastUpdateTime(sdf.parse("2017-08-08 01:34:56.000"))
                .deleteTime(sdf.parse("2018-02-03 11:11:11.022"));
        r8 = _ref(ctx.nextRefId()).title("cover chairs").content("size").url("http://www.chaircovers.com")
                .sys("Chair").creationUser("Haim hania").creationTime(sdf.parse("1999-12-06 12:24:36.666"))
                .lastUpdateUser("Fargon").lastUpdateTime(sdf.parse("2022-12-31 15:34:56.000"))
                .deleteTime(sdf.parse("2023-02-03 11:11:11.022"));

        Assert.assertEquals(8, commit(ctx, REF_INDEX, r1, r2, r3, r4, r5, r6, r7, r8));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testEqReferenceByTitle() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "title", Constraint.of(ConstraintOp.eq, r6.title))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r6.toEntity()).withEntity(r7.toEntity())  //context entity
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceById() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "id", Constraint.of(ConstraintOp.eq, r1.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r1.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class)
    public void testEqReferenceByContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.eq, r1.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r1.toEntity()).withEntity(r3.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByUrl() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "url", Constraint.of(ConstraintOp.eq, r2.url))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r2.toEntity()).withEntity(r5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceBySystem() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "system", Constraint.of(ConstraintOp.eq, r2.system))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r2.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByCreationUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "creationUser", Constraint.of(ConstraintOp.eq, r1.creationUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r1.toEntity()).withEntity(r8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.eq, r1.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r1.toEntity()).withEntity(r3.toEntity()).withEntity(r5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByLastUpdateUser() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "lastUpdateUser", Constraint.of(ConstraintOp.eq, r1.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r1.toEntity()).withEntity(r3.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByLastUpdateTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.eq, r4.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r4.toEntity()).withEntity(r5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByDeleteTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "deleteTime", Constraint.of(ConstraintOp.eq, r7.deleteTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r1.toEntity()).withEntity(r3.toEntity())
                        .withEntity(r5.toEntity()).withEntity(r7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByIdAndContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, r8.content)),
                        new EProp(4, "id", Constraint.of(ConstraintOp.eq, r8.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqReferenceByTitleAndContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, r6.content)),
                        new EProp(4, "title", Constraint.of(ConstraintOp.eq, r6.title))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class)
    public void testEqReferenceByTitleContentAndDeleteTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, r3.content)),
                        new EProp(4, "title", Constraint.of(ConstraintOp.eq, r3.title)),
                        new EProp(4, "deleteTime", Constraint.of(ConstraintOp.eq, r3.deleteTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(r1.toEntity()).withEntity(r3.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


}
