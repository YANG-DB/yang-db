package com.kayhut.fuse.assembly.knowledge;


import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.RvalueBuilder;
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
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.kayhut.fuse.assembly.knowledge.domain.RvalueBuilder._r;
import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


public class KnowledgeSimpleRelationAndRvalueWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static RelationBuilder rel1, rel2, rel3, rel4, rel5;
    static RvalueBuilder rv1, rv2, rv3, rv4, rv5, rv6, rv7, rv8, rv9;
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
                .lastUpdateUser("Yael pery").creationTime(sdf.parse("2010-04-31 11:04:29.089"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:41.489")).deleteTime(sdf.parse("2010-09-09 19:19:11.999"));
        rel4 = _rel(ctx.nextRelId()).ctx("Quantity of wheels").cat("wheels").creationUser("Yaacov Gabuy")
                .lastUpdateUser("Meir Pery").creationTime(sdf.parse("1999-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:42.489")).deleteTime(sdf.parse("2008-08-08 88:88:88.888"));
        rel5 = _rel(ctx.nextRelId()).ctx("Quantity of Wheels").cat("Wheels").creationUser("Yaacov")
                .lastUpdateUser("Moshe").creationTime(sdf.parse("2009-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2006-06-07 05:45:55.565")).deleteTime(sdf.parse("2004-02-03 11:11:11.022"));
        // Rvalues for tests
        rv1 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Volvo").value(2018).bdt("manufacturer").ctx("Car company")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987")).deleteTime(sdf.parse("2018-07-12 09:01:03.764"));
        rv2 = _r(ctx.nextRvalueId()).relId(rv1.relationId).field("Audi").value(2025).bdt("Manufacturer").ctx("Car Company")
                .creationUser("kobi shaul").lastUpdateUser("Dudu Peretz").creationTime(sdf.parse("2019-09-02 10:51:53.563"))
                .deleteTime(sdf.parse("2019-09-02 10:51:53.564"));
        rv3 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Volvo").value("family").bdt("Manufacturer").ctx("Car company")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("1999-04-14 04:41:43.443"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987")).deleteTime(sdf.parse("2010-01-11 01:11:13.161"));
        rv4 = _r(ctx.nextRvalueId()).relId(rv1.relationId).field("audi").value(2025).bdt("Manufacturer").ctx("Cars Company")
                .creationUser("kobi Dudi shaul").lastUpdateUser("Dudi Peretz").creationTime(sdf.parse("2016-12-24 14:54:43.463"))
                .deleteTime(sdf.parse("2019-09-02 10:51:53.564"));
        rv5 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("volvo").value("expensive").bdt("Company").ctx("Car type")
                .creationUser("Avi Shaul").lastUpdateUser("Liran peretz").creationTime(sdf.parse("1981-04-21 13:21:53.003"))
                .lastUpdateTime(sdf.parse("2019-04-17 23:59:58.987")).deleteTime(sdf.parse("1985-07-10 01:11:13.161"));
        rv6 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("date").value(sdf.parse("2017-12-13 11:01:31.121"))
                .bdt("Purchase date").ctx("Date you bought the vehicle").creationUser("Avi Shaul").lastUpdateUser("Liran peretz")
                .creationTime(sdf.parse("1981-04-21 13:21:53.003")).deleteTime(sdf.parse("1985-07-10 01:11:13.161"));
        rv7 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Date").value(sdf.parse("2000-10-03 10:00:00.000"))
                .bdt("Purchase date").ctx("Date you sold the vehicle").creationUser("Avi Shaul").lastUpdateUser("Liran Peretz")
                .lastUpdateTime(sdf.parse("2018-09-17 23:59:58.987")).creationTime(sdf.parse("1983-08-17 17:27:57.707"))
                .deleteTime(sdf.parse("1987-07-16 06:16:16.166"));
        rv8 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Dodge").value("Family").bdt("company").ctx("Car Type")
                .creationUser("Gbi Levi").lastUpdateUser("Oron Lamed").creationTime(sdf.parse("2001-05-15 05:55:55.445"))
                .deleteTime(sdf.parse("2010-01-11 01:11:13.161"));
        rv9 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Dodge").value("Family").bdt("company").ctx("Car Type")
                .creationUser("Gbi Levi").lastUpdateUser("Oron Lamed").creationTime(sdf.parse("2001-05-15 05:55:55.445"))
                .deleteTime(sdf.parse("2010-01-11 01:11:13.161"));
        // Add Relation between two Entities (Relation and Rvalue)
        rel1.value(rv1, rv2, rv3);
        rel2.value(rv4, rv5);
        rel3.value(rv6);
        rel4.value(rv7);
        rel5.value(rv8, rv9);
        // Insert Relation and Rvalue entities to ES
        Assert.assertEquals(5, commit(ctx, REL_INDEX, rel1, rel2, rel3, rel4, rel5));
        Assert.assertEquals(9, commit(ctx, REL_INDEX, rv1, rv2, rv3, rv4, rv5, rv6, rv7, rv8, rv9));
    }


    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper


    // Start Tests:
    @Test
    public void testEqByRelationCategoryAndRvalueFieldId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(4, "hasRvalue", R, null, 5, 0),
                        new ETyped(5, "R", "Rvalue", 6, 0),
                        new EProp(6, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRvalueFieldIdAndRelation() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, rv8.fieldId)),
                        new Rel(4, "hasRvalue", L, null, 5, 0),
                        new ETyped(5, "A", "Relation", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel5.toEntity())
                        .withEntity(rv8.toEntity())
                        .withEntity(rv9.toEntity())
                        .withRelationships(rel5.withRelations("hasRvalue", rv8.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRelationCategoryAndRvalue() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel3.category)),
                        new Rel(4, "hasRvalue", R, null, 5, 0),
                        new ETyped(5, "V", "Rvalue", 6, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity()).withEntity(rel5.toEntity())
                        .withEntity(rv6.toEntity()).withEntity(rv8.toEntity()).withEntity(rv9.toEntity())
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv8.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRvalueFieldIdAndRelationLastUpdateUser() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "V", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, rv5.fieldId)),
                        new Rel(4, "hasRvalue", L, null, 5, 0),
                        new ETyped(5, "A", "Relation", 6, 0),
                        new EProp(6, "lastUpdateUser", Constraint.of(ConstraintOp.eq, rel2.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity())
                        .withEntity(rv5.toEntity())
                        .withRelationships(rel2.withRelations("hasRvalue", rv5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByStrongRelationCategoryAndWeakRvalueFieldId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel3.category)),
                        new Rel(4, "hasRvalue", R, null, 5, 0),
                        new ETyped(5, "R", "Rvalue", 6, 0),
                        new EProp(6, "fieldId", Constraint.of(ConstraintOp.eq, rv6.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity())
                        .withEntity(rv6.toEntity())
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRelationCategoryAndRvalueContainsFieldId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(4, "hasRvalue", R, null, 5, 0),
                        new ETyped(5, "R", "Rvalue", 6, 0),
                        new EProp(6, "fieldId", Constraint.of(ConstraintOp.like, "*ud*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity())
                        .withEntity(rv2.toEntity())
                        .withRelationships(rel1.withRelations("hasRvalue", rv2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRvalueFieldIdAndRelationContainsCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, rv5.fieldId)),
                        new Rel(4, "hasRvalue", L, null, 5, 0),
                        new ETyped(5, "A", "Relation", 6, 0),
                        new EProp(6, "category", Constraint.of(ConstraintOp.like, "*ar*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity())
                        .withEntity(rv5.toEntity())
                        .withRelationships(rel2.withRelations("hasRvalue", rv5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalRvalueFieldId_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*heel*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", R, null, 6, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, rv8.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity()).withEntity(rel4.toEntity()).withEntity(rel5.toEntity())
                        .withEntity(rv8.toEntity()).withEntity(rv9.toEntity())
                        .withRelationships(rel5.withRelations("hasRvalue", rv8.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalRvalueFieldId_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*whe*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", R, null, 6, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, rv7.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel4.toEntity())
                        .withEntity(rv7.toEntity())
                        .withRelationships(rel4.withRelations("hasRvalue", rv7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalRvalueFieldId_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*car*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", R, null, 6, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, rv7.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel2.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryAndOptionalRvalue() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*eels")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", R, null, 6, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity()).withEntity(rel4.toEntity()).withEntity(rel5.toEntity())
                        .withEntity(rv6.toEntity()).withEntity(rv7.toEntity())
                        .withEntity(rv8.toEntity()).withEntity(rv9.toEntity())
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .withRelationships(rel4.withRelations("hasRvalue", rv7.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv8.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRvalueFieldIdAndOptionalRelationCategory_PartExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "V", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*udi*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", L, null, 6, 0),
                        new ETyped(6, "R", "Relation", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity())
                        .withEntity(rv2.toEntity())
                        .withEntity(rv4.toEntity())
                        .withRelationships(rel1.withRelations("hasRvalue", rv2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRvalueFieldIdAndOptionalRelationCategory_NothingExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*olv*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", L, null, 6, 0),
                        new ETyped(6, "A", "Relation", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel4.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rv1.toEntity()).withEntity(rv3.toEntity()).withEntity(rv5.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRvalueFieldIdAndOptionalRelationCategory_AllExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "V", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*olv*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", L, null, 6, 0),
                        new ETyped(6, "A", "Relation", 7, 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.like, "*ar*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel1.toEntity()).withEntity(rel2.toEntity())
                        .withEntity(rv1.toEntity()).withEntity(rv3.toEntity()).withEntity(rv5.toEntity())
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .withRelationships(rel2.withRelations("hasRvalue", rv5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRvalueFieldIdAndOptionalRelation() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "V", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*at*")),
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", L, null, 6, 0),
                        new ETyped(6, "A", "Relation", 7, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity()).withEntity(rel4.toEntity())
                        .withEntity(rv6.toEntity()).withEntity(rv7.toEntity())
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .withRelationships(rel4.withRelations("hasRvalue", rv7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryLastUpdateUserAndOptionalRvalueFieldId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*hee*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, rel3.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasRvalue", R, null, 7, 0),
                        new ETyped(7, "R", "Rvalue", 8, 0),
                        new EProp(8, "fieldId", Constraint.of(ConstraintOp.eq, rv6.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity())
                        .withEntity(rv6.toEntity())
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByRelationCategoryLastUpdateUserAndOptionalRvalueFieldId_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*eel*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, rel3.lastUpdateUser)),
                        new OptionalComp(5, 6),
                        new Rel(6, "hasRvalue", R, null, 7, 0),
                        new ETyped(7, "R", "Rvalue", 8, 0),
                        new EProp(8, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel3.toEntity())
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByRvalueFieldIdAndRelation_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "V", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.eq, "carss")),
                        new Rel(4, "hasRvalue", L, null, 5, 0),
                        new ETyped(5, "R", "Relation", 6, 0)
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
