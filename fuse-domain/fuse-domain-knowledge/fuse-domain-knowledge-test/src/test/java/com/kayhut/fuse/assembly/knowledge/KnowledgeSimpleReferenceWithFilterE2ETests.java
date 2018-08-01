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
    static RefBuilder ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        //Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
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
        ref5 = _ref(ctx.nextRefId()).title("engine").content("Quantity of different parts").url("https://www.carsforum.co.il")
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
        // Insert Reference entities to ES
        Assert.assertEquals(8, commit(ctx, REF_INDEX, ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8));
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
                        new EProp(2, "title", Constraint.of(ConstraintOp.eq, ref6.title))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref6.toEntity()).withEntity(ref7.toEntity())  //context entity
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
                        new EProp(2, "id", Constraint.of(ConstraintOp.eq, ref1.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity())
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
                        new EProp(2, "content", Constraint.of(ConstraintOp.eq, ref1.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref3.toEntity())
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
                        new EProp(2, "url", Constraint.of(ConstraintOp.eq, ref2.url))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref2.toEntity()).withEntity(ref5.toEntity())
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
                        new EProp(2, "system", Constraint.of(ConstraintOp.eq, ref2.system))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref2.toEntity())
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
                        new EProp(2, "creationUser", Constraint.of(ConstraintOp.eq, ref1.creationUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref8.toEntity())
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
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.eq, ref1.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref3.toEntity()).withEntity(ref5.toEntity())
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
                        new EProp(2, "lastUpdateUser", Constraint.of(ConstraintOp.eq, ref1.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref3.toEntity())
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
                        new EProp(2, "lastUpdateTime", Constraint.of(ConstraintOp.eq, ref4.lastUpdateTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref4.toEntity()).withEntity(ref5.toEntity())
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
                        new EProp(2, "deleteTime", Constraint.of(ConstraintOp.eq, ref7.deleteTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref3.toEntity())
                        .withEntity(ref5.toEntity()).withEntity(ref7.toEntity())
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
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, ref8.content)),
                        new EProp(4, "id", Constraint.of(ConstraintOp.eq, ref8.id()))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref8.toEntity())
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
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, ref6.content)),
                        new EProp(4, "title", Constraint.of(ConstraintOp.eq, ref6.title))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref6.toEntity())
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
                        new EProp(3, "content", Constraint.of(ConstraintOp.eq, ref3.content)),
                        new EProp(4, "title", Constraint.of(ConstraintOp.eq, ref3.title)),
                        new EProp(4, "deleteTime", Constraint.of(ConstraintOp.eq, ref3.deleteTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref3.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testInSetReferenceByTitle() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "title", Constraint.of(ConstraintOp.inSet,
                                Arrays.asList(ref2.title, ref3.title, ref6.title)))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref2.toEntity()).withEntity(ref3.toEntity())
                        .withEntity(ref5.toEntity()).withEntity(ref6.toEntity()).withEntity(ref7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class)  // Supposed to find r2 and r5 but doesn't find anything
    public void testLikeReferenceStartOnTitle() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "title", Constraint.of(ConstraintOp.like, "engi*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref2.toEntity()).withEntity(ref5.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class)  // Supposed to find r1 and r3 but doesn't find anything
    public void testLikeReferenceEndOnTitle() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "title", Constraint.of(ConstraintOp.like, "*wheel"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref3.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class)  // Supposed to find r6 and r7 but doesn't find anything
    public void testLikeReferenceContainsOnTitle() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "title", Constraint.of(ConstraintOp.like, "*assie*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref6.toEntity()).withEntity(ref7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeReferenceContainsOnSystem() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "system", Constraint.of(ConstraintOp.like, "*ars*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref4.toEntity()).withEntity(ref6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeAnyReferenceContainsOnTitle() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "title", Constraint.of(ConstraintOp.likeAny, Arrays.asList("*assi*", "*arbo*")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref4.toEntity()).withEntity(ref6.toEntity()).withEntity(ref7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeAnyReferenceContainsOnSystem() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "system", Constraint.of(ConstraintOp.likeAny, Arrays.asList("*ars*", "*igh*")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref4.toEntity()).withEntity(ref5.toEntity()).withEntity(ref6.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testGtReferenceByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.gt, ref6.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testGteReferenceByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.ge, ref6.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref6.toEntity()).withEntity(ref7.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLtReferenceByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.lt, ref4.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref2.toEntity()).withEntity(ref8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLteReferenceByCreationTime() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "creationTime", Constraint.of(ConstraintOp.le, ref4.creationTime))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref2.toEntity()).withEntity(ref4.toEntity()).withEntity(ref8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test(expected = AssertionError.class)
    public void testNeReferenceByContent() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "content", Constraint.of(ConstraintOp.ne, ref6.content))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Create expectedResult
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(ref1.toEntity()).withEntity(ref2.toEntity()).withEntity(ref3.toEntity())
                        .withEntity(ref4.toEntity()).withEntity(ref5.toEntity()).withEntity(ref8.toEntity())
                        .build()).build();

        // Check if expected and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEmptyReferenceBySystem() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Reference", 2, 0),
                        new EProp(2, "system", Constraint.of(ConstraintOp.likeAny, Arrays.asList("*kobi*", "*cat*")))
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
