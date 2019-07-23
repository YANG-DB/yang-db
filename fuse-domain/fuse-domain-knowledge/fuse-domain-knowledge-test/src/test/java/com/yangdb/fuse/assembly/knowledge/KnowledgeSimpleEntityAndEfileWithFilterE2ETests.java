package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.FileBuilder;
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
import static com.yangdb.fuse.assembly.knowledge.domain.FileBuilder._f;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


public class KnowledgeSimpleEntityAndEfileWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4;
    static FileBuilder f1, f2, f3, f4, f5, f6, f7, f8;
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
        // Efile entities for tests
        f1 = _f(ctx.nextFileId()).name("mazda").display("mazda").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        f2 = _f(ctx.nextFileId()).name("Opel").display("Opel").path("https://www.google.co.il").mime("string").cat("new car").ctx("Family cars")
                .desc("search opel at google").creationUser("Haim hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("dudi Fargon").lastUpdateTime(sdf.parse("2017-06-01 12:34:56.789"))
                .deleteTime(sdf.parse("2018-01-01 12:12:12.122"));
        f3 = _f(ctx.nextFileId()).name("mazda").display("mazda").path("http://www.baeldung.com").mime("String").cat("cars").ctx("Family Cars")
                .desc("search for mazda").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.727"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2017-05-01 12:34:56.789"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        f4 = _f(ctx.nextFileId()).name("Mazda").display("Mazda").path("http://www.baeldung.com/java-tuples").mime("Integer").cat("New Cars")
                .ctx("family cars").desc("Mazda at google").creationUser("haim Hania")
                .creationTime(sdf.parse("2014-03-01 01:01:01.111")).lastUpdateUser("Dudi Fargon")
                .lastUpdateTime(sdf.parse("2016-06-01 12:34:56.789")).deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        f5 = _f(ctx.nextFileId()).name("Opel").display("Opel").path("https://www.google.co.il/search?source=hp&ei").mime("integer")
                .cat("Cars").ctx("family cars").desc("Opel at google").creationUser("Haim")
                .creationTime(sdf.parse("2014-03-01 01:01:01.111")).lastUpdateUser("Dudi")
                .lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000")).deleteTime(sdf.parse("2010-03-03 03:03:02.333"));
        f6 = _f(ctx.nextFileId()).name("Opel").display("Opel").path("https://en.wikipedia.org").mime("int").cat("cars").ctx("all cars")
                .desc("opel at google").creationUser("Hania").creationTime(sdf.parse("2015-12-11 11:05:05.543"))
                .lastUpdateUser("Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2008-12-12 12:12:12.129"));
        f7 = _f(ctx.nextFileId()).name("BMW").display("BMW").path("https://en.wikipedia.org/wiki/Citroen").mime("Int").cat("car")
                .ctx("All cars").desc("Citroen search").creationUser("Haim Hania")
                .creationTime(sdf.parse("2016-11-12 12:11:10.123")).lastUpdateUser("Fargon")
                .lastUpdateTime(sdf.parse("2009-04-16 00:00:00.000")).deleteTime(sdf.parse("2005-10-10 10:10:10.101"));
        f8 = _f(ctx.nextFileId()).name("BMW").display("BMW").path("https://en.wikipedia.org/wiki/Citroen").mime("Int").cat("car")
                .ctx("All cars").desc("Citroen search").creationUser("Haim Hania")
                .creationTime(sdf.parse("2016-11-12 12:11:10.123")).lastUpdateUser("Fargon")
                .lastUpdateTime(sdf.parse("2009-04-16 00:00:00.000")).deleteTime(sdf.parse("2005-10-10 10:10:10.101"));
        // Add Efile to Entity
        e1.file(f1);
        e1.file(f2);
        e2.file(f3);
        e2.file(f4);
        e2.file(f5);
        e3.file(f6);
        e4.file(f7);
        e4.file(f8);
        // Insert Entity and Evalue entities to ES
        Assert.assertEquals(4, commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals(8, commit(ctx, INDEX, f1, f2, f3, f4, f5, f6, f7, f8));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Ignore
    @Test
    public void testEqByEntityCategoryAndEfileName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEfile", R, null, 5, 0),
                        new ETyped(5, "F", "Efile", 6, 0),
                        new EProp(6, "name", Constraint.of(ConstraintOp.eq, f1.name))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(f1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(f3.toEntity())
                        .withRelationships(e1.withRelations("hasEfile", f1.id()))
                        .withRelationships(e2.withRelations("hasEfile", f3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEfileNameAndEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.eq, f3.name)),
                        new Rel(4, "hasEfile", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(f1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(f3.toEntity())
                        .withRelationships(e1.withRelations("hasEfile", f1.id()))
                        .withRelationships(e2.withRelations("hasEfile", f3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndEfile() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e4.category)),
                        new Rel(4, "hasEfile", R, null, 5, 0),
                        new ETyped(5, "F", "Efile", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(f7.toEntity())
                        .withEntity(f8.toEntity())
                        .withRelationships(e4.withRelations("hasEfile", f7.id()))
                        .withRelationships(e4.withRelations("hasEfile", f8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEfileNameAndEntityLastUpdateUser() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.eq, f7.name)),
                        new Rel(4, "hasEfile", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e4.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(f7.toEntity())
                        .withEntity(f8.toEntity())
                        .withRelationships(e4.withRelations("hasEfile", f7.id()))
                        .withRelationships(e4.withRelations("hasEfile", f8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByStrongEntityCategoryAndWeakEfileName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e4.category)),
                        new Rel(4, "hasEfile", R, null, 5, 0),
                        new ETyped(5, "F", "Efile", 6, 0),
                        new EProp(6, "name", Constraint.of(ConstraintOp.eq, f8.name))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(f7.toEntity())
                        .withEntity(f8.toEntity())
                        .withRelationships(e4.withRelations("hasEfile", f7.id()))
                        .withRelationships(e4.withRelations("hasEfile", f8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndEfileContainsName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e2.category)),
                        new Rel(4, "hasEfile", R, null, 5, 0),
                        new ETyped(5, "F", "Efile", 6, 0),
                        new EProp(6, "name", Constraint.of(ConstraintOp.like, "*azd*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e2.toEntity())
                        .withEntity(f3.toEntity())
                        .withEntity(f4.toEntity())
                        .withEntity(e1.toEntity())
                        .withEntity(f1.toEntity())
                        .withRelationships(e2.withRelations("hasEfile", f3.id()))
                        .withRelationships(e2.withRelations("hasEfile", f4.id()))
                        .withRelationships(e1.withRelations("hasEfile", f1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEfileNameAndEntityContainsCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.eq, f1.name)),
                        new Rel(4, "hasEfile", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.like, "*pe*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(f1.toEntity())
                        .withEntity(f3.toEntity())
                        .withRelationships(e1.withRelations("hasEfile", f1.id()))
                        .withRelationships(e2.withRelations("hasEfile", f3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalEfileName_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "ope*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", R, null, 6, 0),
                        new ETyped(6, "F", "Efile", 7, 0),
                        new EProp(7, "name", Constraint.of(ConstraintOp.eq, f1.name))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(f1.toEntity())
                        .withEntity(f3.toEntity())
                        .withRelationships(e1.withRelations("hasEfile", f1.id()))
                        .withRelationships(e2.withRelations("hasEfile", f3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalEfileName_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*itroe*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", R, null, 6, 0),
                        new ETyped(6, "F", "Efile", 7, 0),
                        new EProp(7, "name", Constraint.of(ConstraintOp.eq, f6.name))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(f6.toEntity())
                        .withRelationships(e3.withRelations("hasEfile", f6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalEfileName_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*azd*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", R, null, 6, 0),
                        new ETyped(6, "F", "Efile", 7, 0),
                        new EProp(7, "name", Constraint.of(ConstraintOp.eq, f1.name))
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
    public void testLikeByEntityCategoryAndOptionalEfile() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*maz*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", R, null, 6, 0),
                        new ETyped(6, "F", "Efile", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(f7.toEntity())
                        .withEntity(f8.toEntity())
                        .withRelationships(e4.withRelations("hasEfile", f7.id()))
                        .withRelationships(e4.withRelations("hasEfile", f8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEfileNameAndOptionalEntityCategory_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.like, "*pe*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(f2.toEntity())
                        .withEntity(f5.toEntity())
                        .withEntity(f6.toEntity())
                        .withRelationships(e1.withRelations("hasEfile", f2.id()))
                        .withRelationships(e2.withRelations("hasEfile", f5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEfileNameAndOptionalEntityCategory_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.like, "*BM*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e3.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(f7.toEntity())
                        .withEntity(f8.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEfileNameAndOptionalEntityCategory_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.like, "*BM*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e4.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(f7.toEntity())
                        .withEntity(f8.toEntity())
                        .withRelationships(e4.withRelations("hasEfile", f7.id()))
                        .withRelationships(e4.withRelations("hasEfile", f8.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEfileNameAndOptionalEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.like, "*pe*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasEfile", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity()).withEntity(e3.toEntity())
                        .withEntity(f2.toEntity()).withEntity(f5.toEntity()).withEntity(f6.toEntity())
                        .withRelationships(e1.withRelations("hasEfile", f2.id()))
                        .withRelationships(e2.withRelations("hasEfile", f5.id()))
                        .withRelationships(e3.withRelations("hasEfile", f6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalEfileName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*roe*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e3.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasEfile", R, null, 7, 0),
                        new ETyped(7, "F", "Efile", 8, 0),
                        new EProp(8, "name", Constraint.of(ConstraintOp.eq, f6.name))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(f6.toEntity())
                        .withRelationships(e3.withRelations("hasEfile", f6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalEfileName_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*tro*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e3.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasEfile", R, null, 7, 0),
                        new ETyped(7, "F", "Efile", 8, 0),
                        new EProp(8, "name", Constraint.of(ConstraintOp.eq, f7.name))
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
    public void testLikeByEfileNameAndEntity_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "F", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "name", Constraint.of(ConstraintOp.like, "*kobishaul*")),
                        new Rel(4, "hasEfile", L, null, 5, 0),
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
