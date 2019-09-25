package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.RvalueBuilder;
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
import java.util.Collections;
import java.util.TimeZone;
import static com.yangdb.fuse.assembly.knowledge.Setup.client;
import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.knowledge.Setup.manager;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.domain.RvalueBuilder._r;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


public class KnowledgeSimpleEntityRelationAndRvalueWithFilterE2ETests {


    static KnowledgeWriterContext ctx;
    static RelationBuilder rel1, rel2, rel3, rel4, rel5;
    static RvalueBuilder rv1, rv2, rv3, rv4, rv5, rv6, rv7, rv8, rv9;
    static EntityBuilder e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
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
                .creationUser("Avi Shaul").lastUpdateUser("Dudi Peretz").creationTime(sdf.parse("1981-04-21 13:21:53.003"))
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
        // Add Relation between two Entities and between Relation and Rvalue
        rel1.sideA(e1).sideB(e2).value(rv1, rv2, rv3);
        e1.rel(rel1, "out");
        e2.rel(rel1, "in");
        rel2.sideA(e3).sideB(e4).value(rv4, rv5);
        e3.rel(rel2, "out");
        e4.rel(rel2, "in");
        rel3.sideA(e5).sideB(e6).value(rv6);
        e5.rel(rel3, "out");
        e6.rel(rel3, "in");
        rel4.sideA(e7).sideB(e8).value(rv7);
        e7.rel(rel4, "out");
        e8.rel(rel4, "in");
        rel5.sideA(e9).sideB(e10).value(rv8, rv9);
        e9.rel(rel5, "out");
        e10.rel(rel5, "in");
        // Insert Relation and Rvalue entities to ES
        Assert.assertEquals(20, commit(ctx, INDEX, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));
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
    public void testEqByEntityCategoryAndRelationCategoryAndRvalueFieldId_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R1", "Relation", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(8, "hasRvalue", R, null, 9, 0),
                        new ETyped(9, "R", "Rvalue", 10, 0),
                        new EProp(10, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity()).withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndRelationCategoryAndRvalueFieldId_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(4, "hasRvalue", R, null, 6, 0),
                        new Rel(5, "hasRelation", L, null, 8, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId)),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.eq, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndRelationCategoryAndRvalue_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R1", "Relation", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(8, "hasRvalue", R, null, 9, 0),
                        new ETyped(9, "R", "Rvalue", 10, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity()).withEntity(rv2.toEntity()).withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv2.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndRelationCategoryAndRvalue_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(4, "hasRvalue", R, null, 6, 0),
                        new Rel(5, "hasRelation", L, null, 7, 0),
                        new ETyped(6, "R", "Rvalue", 9, 0),
                        new ETyped(7, "A", "Entity", 8, 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity()).withEntity(rv2.toEntity()).withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv2.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityCategoryAndRelationAndRvalueFieldId_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R1", "Relation", 6, 0),
                        new Quant1(6, QuantType.all, Collections.singletonList(7) , 0),
                        new Rel(7, "hasRvalue", R, null, 8, 0),
                        new ETyped(8, "R", "Rvalue", 9, 0),
                        new EProp(9, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityCategoryAndRelationAndRvalueFieldId_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new Rel(3, "hasRvalue", R, null, 5, 0),
                        new Rel(4, "hasRelation", L, null, 7, 0),
                        new ETyped(5, "R", "Rvalue", 6, 0),
                        new EProp(6, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId)),
                        new ETyped(7, "A", "Entity", 8, 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityAndRelationCategoryAndRvalueLastUpdateUser_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Rel(2, "hasRelation", R, null, 3, 0),
                        new ETyped(3, "R1", "Relation", 4, 0),
                        new Quant1(4, QuantType.all, Arrays.asList(5, 6), 0),
                        new EProp(5, "category", Constraint.of(ConstraintOp.eq, rel2.category)),
                        new Rel(6, "hasRvalue", R, null, 7, 0),
                        new ETyped(7, "R", "Rvalue", 8, 0),
                        new EProp(8, "lastUpdateUser", Constraint.of(ConstraintOp.eq, rv4.lastUpdateUser))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity()).withEntity(e4.toEntity())
                        .withEntity(rel2.toEntity())
                        .withEntity(rv4.toEntity())
                        .withEntity(rv5.toEntity())
                        .withRelationships(e3.withRelations("hasRelation", rel2.id()))
                        .withRelationships(e4.withRelations("hasRelation", rel2.id()))
                        .withRelationships(rel2.withRelations("hasRvalue", rv4.id()))
                        .withRelationships(rel2.withRelations("hasRvalue", rv5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityAndRelationCategoryAndRvalueLastUpdateUser_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel2.category)),
                        new Rel(4, "hasRvalue", R, null, 6, 0),
                        new Rel(5, "hasRelation", L, null, 8, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "lastUpdateUser", Constraint.of(ConstraintOp.eq, rv4.lastUpdateUser)),
                        new ETyped(8, "A", "Entity", 9, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e3.toEntity()).withEntity(e4.toEntity())
                        .withEntity(rel2.toEntity())
                        .withEntity(rv4.toEntity())
                        .withEntity(rv5.toEntity())
                        .withRelationships(e3.withRelations("hasRelation", rel2.id()))
                        .withRelationships(e4.withRelations("hasRelation", rel2.id()))
                        .withRelationships(rel2.withRelations("hasRvalue", rv4.id()))
                        .withRelationships(rel2.withRelations("hasRvalue", rv5.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityAndRelationCategoryAndRvalueFieldId_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Rel(2, "hasRelation", R, null, 3, 0),
                        new ETyped(3, "R1", "Relation", 4, 0),
                        new Quant1(4, QuantType.all, Arrays.asList(5, 6), 0),
                        new EProp(5, "category", Constraint.of(ConstraintOp.eq, rel3.category)),
                        new Rel(6, "hasRvalue", R, null, 7, 0),
                        new ETyped(7, "R", "Rvalue", 8, 0),
                        new EProp(8, "fieldId", Constraint.of(ConstraintOp.eq, rv6.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity()).withEntity(e6.toEntity())
                        .withEntity(rel3.toEntity())
                        .withEntity(rv6.toEntity())
                        .withRelationships(e5.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testEqByEntityAndRelationCategoryAndRvalueFieldId_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel3.category)),
                        new Rel(4, "hasRvalue", R, null, 6, 0),
                        new Rel(5, "hasRelation", L, null, 8, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, rv6.fieldId)),
                        new ETyped(8, "A", "Entity", 9, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity()).withEntity(e6.toEntity())
                        .withEntity(rel3.toEntity())
                        .withEntity(rv6.toEntity())
                        .withRelationships(e5.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityCategoryAndRelationContainsCategoryAndRvalueFieldId_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R1", "Relation", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.like, "*ar*")),
                        new Rel(8, "hasRvalue", R, null, 9, 0),
                        new ETyped(9, "R", "Rvalue", 10, 0),
                        new EProp(10, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndRelationContainsCategoryAndRvalueFieldId_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*ars")),
                        new Rel(4, "hasRvalue", R, null, 6, 0),
                        new Rel(5, "hasRelation", L, null, 8, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId)),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.eq, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityCategoryAndRelationCategoryAndRvalueContainsFieldId_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R1", "Relation", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(8, "hasRvalue", R, null, 9, 0),
                        new ETyped(9, "R", "Rvalue", 10, 0),
                        new EProp(10, "fieldId", Constraint.of(ConstraintOp.like, "*olv*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryAndRelationCategoryAndRvalueContainsFieldId_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(4, "hasRvalue", R, null, 6, 0),
                        new Rel(5, "hasRelation", L, null, 8, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.like, "*olv*")),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.eq, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityContainsCategoryAndRelationCategoryAndRvalueFieldId_Chain() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*pel")),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R1", "Relation", 6, 0),
                        new Quant1(6, QuantType.all, Arrays.asList(7, 8), 0),
                        new EProp(7, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(8, "hasRvalue", R, null, 9, 0),
                        new ETyped(9, "R", "Rvalue", 10, 0),
                        new EProp(10, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void tesLikeByEntityContainsCategoryAndRelationCategoryAndRvalueFieldId_Tree() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new Rel(4, "hasRvalue", R, null, 6, 0),
                        new Rel(5, "hasRelation", L, null, 8, 0),
                        new ETyped(6, "R", "Rvalue", 7, 0),
                        new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq, rv1.fieldId)),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.like, "*pel"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByOptionalEntityCategoryAndOptionalRelationCategoryAndRvalueContainsFieldId() throws IOException, InterruptedException {
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
                        new ETyped(6, "R1", "Relation", 7, 0),
                        new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, rel1.category)),
                        new OptionalComp(9, 10),
                        new Rel(10, "hasRelation", L, null, 11, 0),
                        new ETyped(11, "A", "Entity", 12, 0),
                        new EProp(12, "category", Constraint.of(ConstraintOp.like, e1.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withEntity(rv5.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testLikeByEntityContainsCategoryAndOptionalRelationAndRvalueContainsFieldId() throws IOException, InterruptedException {
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
                        new ETyped(6, "R1", "Relation", 7, 0),
                        new Rel(7, "hasRelation", L, null, 8, 0),
                        new ETyped(8, "A", "Entity", 9, 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.like, "*ope*"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity()).withEntity(e2.toEntity())
                        .withEntity(rel1.toEntity())
                        .withEntity(rv1.toEntity())
                        .withEntity(rv3.toEntity())
                        .withEntity(rv5.toEntity())
                        .withRelationships(e1.withRelations("hasRelation", rel1.id()))
                        .withRelationships(e2.withRelations("hasRelation", rel1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv1.id()))
                        .withRelationships(rel1.withRelations("hasRvalue", rv3.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void tesLikeByEntityContainsCategoryAndRelationContainsCategoryAndOptionalRvalueFieldId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 6), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*els*")),  // 3,4,5
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", R, null, 7, 0),
                        new Rel(6, "hasRelation", L, null, 9, 0),
                        new ETyped(7, "R", "Rvalue", 8, 0),
                        new EProp(8, "fieldId", Constraint.of(ConstraintOp.eq, rv9.fieldId)), // 8,9
                        new ETyped(9, "A", "Entity", 10, 0),
                        new EProp(10, "category", Constraint.of(ConstraintOp.like, "*oyo*")) // 9,10
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e7.toEntity()).withEntity(e9.toEntity()).withEntity(e10.toEntity())
                        .withEntity(rel4.toEntity()).withEntity(rel5.toEntity())
                        .withEntity(rv8.toEntity())
                        .withEntity(rv9.toEntity())
                        .withRelationships(e7.withRelations("hasRelation", rel4.id()))
                        .withRelationships(e9.withRelations("hasRelation", rel5.id()))
                        .withRelationships(e10.withRelations("hasRelation", rel5.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv8.id()))
                        .withRelationships(rel5.withRelations("hasRvalue", rv9.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void tesLikeByOptionalEntityAndRelationContainsCategoryAndOptionalRvalue() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "R1", "Relation", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 6), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*els*")),  // 3,4,5
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRvalue", R, null, 8, 0),
                        new OptionalComp(6, 7),
                        new Rel(7, "hasRelation", L, null, 9, 0),
                        new ETyped(8, "R", "Rvalue", 11, 0),  // 6,7,8,9
                        new ETyped(9, "A", "Entity", 10, 0) // 5,6,7,8,9,10
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e5.toEntity()).withEntity(e6.toEntity()).withEntity(e7.toEntity())
                        .withEntity(e8.toEntity()).withEntity(e9.toEntity()).withEntity(e10.toEntity())
                        .withEntity(rel3.toEntity()).withEntity(rel4.toEntity()).withEntity(rel5.toEntity())
                        .withEntity(rv6.toEntity()).withEntity(rv7.toEntity())
                        .withEntity(rv8.toEntity()).withEntity(rv9.toEntity())
                        .withRelationships(e5.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(e7.withRelations("hasRelation", rel4.id()))
                        .withRelationships(e8.withRelations("hasRelation", rel4.id()))
                        .withRelationships(e9.withRelations("hasRelation", rel5.id()))
                        .withRelationships(e10.withRelations("hasRelation", rel5.id()))
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
    public void testLikeByEntityContainsCategoryAndOptionalRelationCategoryAndRvalueContainsFieldIdLastUpdateUser() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(

                        new Start(0, 1),
                        new ETyped(1, "R", "Rvalue", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "fieldId", Constraint.of(ConstraintOp.like, "*ate*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.like, "*ran Pe*")), // 7
                        new OptionalComp(5, 6),
                        new Rel(6, "hasRvalue", L, null, 7, 0),
                        new ETyped(7, "R1", "Relation", 8, 0),
                        new Quant1(8, QuantType.all, Arrays.asList(9, 10), 0),
                        new EProp(9, "category", Constraint.of(ConstraintOp.eq, rel4.category)), // 4
                        new Rel(10, "hasRelation", L, null, 11, 0),
                        new ETyped(11, "A", "Entity", 12, 0),
                        new EProp(12, "category", Constraint.of(ConstraintOp.like, "*exu*"))  // 8
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e8.toEntity())
                        .withEntity(rel4.toEntity())
                        .withEntity(rv7.toEntity())
                        .withRelationships(e8.withRelations("hasRelation", rel4.id()))
                        .withRelationships(rel4.withRelations("hasRvalue", rv7.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testEqByEntityCategoryAndOptionalRelationCategoryAndRvalue() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e6.category)), // 6,8
                        new OptionalComp(4, 5),
                        new Rel(5, "hasRelation", R, null, 6, 0),
                        new ETyped(6, "R1", "Relation", 7, 0),
                        new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, rel3.category)), // 3
                        new Rel(9, "hasRvalue", R, null, 10, 0),
                        new ETyped(10, "R", "Rvalue", 11, 0) // 6
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e6.toEntity()).withEntity(e8.toEntity())
                        .withEntity(rel3.toEntity())
                        .withEntity(rv6.toEntity())
                        .withRelationships(e6.withRelations("hasRelation", rel3.id()))
                        .withRelationships(rel3.withRelations("hasRvalue", rv6.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

}
