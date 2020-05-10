package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
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
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


public class KnowledgeSimpleEntityAndRelationWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
    static RelationBuilder rel1, rel2, rel3, rel4, rel5;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();//Todo remove while running in Suite Context
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
        e5 = _e(ctx.nextLogicalId()).cat("mitsubishi").ctx("context5").lastUpdateUser("Dudi Frid")
                .creationUser("Kobi Shaul").creationTime(sdf.parse("2018-02-28 23:55:13.899"))
                .lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999")).deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e6 = _e(ctx.nextLogicalId()).cat("lexus").ctx("context6").creationUser("Kobi").lastUpdateUser("Kobi")
                .creationTime(sdf.parse("2018-02-28 23:55:13.899")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e7 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context6").creationUser("Haim").lastUpdateUser("Kobi")
                .creationTime(sdf.parse("2018-02-28 23:55:13.899")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e8 = _e(ctx.nextLogicalId()).cat("lexus").ctx("context7").lastUpdateUser("Haim").creationUser("Yael Gabai")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e9 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context2").lastUpdateUser("Dudi").creationUser("Yael Gabai")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e10 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context10").lastUpdateUser("Kobi").creationUser("Kobi Shaul")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
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
        // Add Relation between two Entities
        rel1.sideA(e1).sideB(e2);
        e1.rel(rel1, "out");
        e2.rel(rel1, "in");
        rel2.sideA(e3).sideB(e4);
        e3.rel(rel2, "out");
        e4.rel(rel2, "in");
        rel3.sideA(e5).sideB(e6);
        e5.rel(rel3, "out");
        e6.rel(rel3, "in");
        rel4.sideA(e7).sideB(e8);
        e7.rel(rel4, "out");
        e8.rel(rel4, "in");
        rel5.sideA(e9).sideB(e10);
        e9.rel(rel5, "out");
        e10.rel(rel5, "in");

        // Insert Entity and Reference entities to ES
        Assert.assertEquals("error loading data ",20, commit(ctx, INDEX, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));
        Assert.assertEquals("error loading data ",5, commit(ctx, REL_INDEX, rel1, rel2, rel3, rel4, rel5));
    }

    @AfterClass
    public static void after() {
        if(ctx!=null) Assert.assertEquals(25,ctx.removeCreated());

    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testEqByEntityCategoryAndRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R", "Relation", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.eq, rel1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(e2.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }
    @Test
    public void testEqByEntityCategoryAndInRelationCategoryAndOutRel() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R", "Relation", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(8, "hasRelation", L, null, 9, 0),
                        new ETyped(9, "BV", "Entity", -1, 0))
                ).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(e2.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRelationCategoryAndEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel3.category)),
                        new Rel(4, "hasRelation", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity())
                        .withEntity(e6.toEntity())
                        .withEntity(rel3.toEntity())
                        .withEntity(e9.toEntity())
                        .withEntity(e10.toEntity())
                        .withEntity(rel5.toEntity())
                        .withRelationships(e5.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e9.withRelations("hasRelation", rel5.id()))
                        .withRelationships(e10.withRelations("hasRelation", rel5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndRelation() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e9.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R", "Relation", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e7.toEntity())
                        .withEntity(e9.toEntity())
                        .withEntity(e10.toEntity())
                        .withEntity(rel4.toEntity())
                        .withEntity(rel5.toEntity())
                        .withRelationships(e7.withRelations("hasRelation", rel4.id()))
                        .withRelationships(e9.withRelations("hasRelation", rel5.id()))
                        .withRelationships(e10.withRelations("hasRelation", rel5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRelationCategoryAndEntityLastUpdateUser() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel2.category)),
                        new Rel(4, "hasRelation", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e3.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity())
                        .withEntity(e4.toEntity())
                        .withEntity(rel2.toEntity())
                        .withRelationships(e3.withRelations("hasRelation", rel2.id()))
                        .withRelationships(e4.withRelations("hasRelation", rel2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByStrongEntityCategoryAndWeakRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e5.category)),
                        new Rel(4, "hasRelation", R, "A->R", 5, 0),
                        new ETyped(5, "R", "Relation", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.eq, rel3.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity())
                        .withEntity(rel3.toEntity())
                        .withRelationships(e5.withRelations("hasRelation", rel3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndRelationContainsCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e7.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R", "Relation", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.like, "*Whe*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e9.toEntity())
                        .withEntity(e10.toEntity())
                        .withEntity(rel5.toEntity())
                        .withRelationships(e9.withRelations("hasRelation", rel5.id()))
                        .withRelationships(e10.withRelations("hasRelation", rel5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRelationCategoryAndEntityContainsCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel3.category)),
                        new Rel(4, "hasRelation", L, null, 5, 0),
                        new ETyped(5, "A", "Entity", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.like, "*exu*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e6.toEntity())
                        .withEntity(rel3.toEntity())
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalRelationCategory_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "toyo*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", R, null, 6, 0),
                        new ETyped(6, "R", "Relation", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel4.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e7.toEntity())
                        .withEntity(e9.toEntity())
                        .withEntity(e10.toEntity())
                        .withEntity(rel4.toEntity())
                        .withRelationships(e7.withRelations("hasRelation", rel4.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalRelationCategory_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*maz*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", R, null, 6, 0),
                        new ETyped(6, "R", "Relation", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e4.toEntity())
                        .withEntity(rel2.toEntity())
                        .withRelationships(e4.withRelations("hasRelation", rel2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndOptionalRelationCategory_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*azd*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", R, null, 6, 0),
                        new ETyped(6, "R", "Relation", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel1.category))
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
    public void testLikeByEntityCategoryAndOptionalRelation() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*lex*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", R, null, 6, 0),
                        new ETyped(6, "R", "Relation", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e6.toEntity())
                        .withEntity(e8.toEntity())
                        .withEntity(rel3.toEntity())
                        .withEntity(rel4.toEntity())
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e8.withRelations("hasRelation", rel4.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalEntityCategory_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*eel*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e6.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e6.toEntity())
                        .withEntity(e8.toEntity())
                        .withEntity(rel3.toEntity())
                        .withEntity(rel4.toEntity())
                        .withEntity(rel5.toEntity())
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e8.withRelations("hasRelation", rel4.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalEntityCategory_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*hee*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e4.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity())
                        .withEntity(rel4.toEntity())
                        .withEntity(rel5.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalEntityCategory_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*Car*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*heel*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", L, null, 6, 0),
                        new ETyped(6, "A", "Entity", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity()).withEntity(e6.toEntity()).withEntity(e7.toEntity())
                        .withEntity(e8.toEntity()).withEntity(e9.toEntity()).withEntity(e10.toEntity())
                        .withEntity(rel3.toEntity()).withEntity(rel4.toEntity()).withEntity(rel5.toEntity())
                        .withRelationships(e5.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e7.withRelations("hasRelation", rel4.id()))
                        .withRelationships(e8.withRelations("hasRelation", rel4.id()))
                        .withRelationships(e9.withRelations("hasRelation", rel5.id()))
                        .withRelationships(e10.withRelations("hasRelation", rel5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*oyot*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e9.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasRelation", R, null, 7, 0),
                        new ETyped(7, "R", "Relation", 8, 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, rel5.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e9.toEntity())
                        .withEntity(rel5.toEntity())
                        .withRelationships(e9.withRelations("hasRelation", rel5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalRelationCategory_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*oyot*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e9.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasRelation", R, null, 7, 0),
                        new ETyped(7, "R", "Relation", 8, 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, rel4.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e9.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndEntity_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*notExist*")),
                        new Rel(4, "hasRelation", L, null, 5, 0),
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
