package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder;
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
import java.util.Date;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


public class KnowledgeEntityAndEvalueWithFilterByLogicalQueryE2ETests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4;
    static ValueBuilder v1, v2, v3, v4, v5, v6, v7, v8, v9, v10;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(true,true);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Entities for tests
        e1 = _e(ctx.nextLogicalId()).cat("opel").ctx("context1").creationTime(sdf.parse("2018-01-28 14:33:53.567"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e2 = _e(ctx.nextLogicalId()).cat("opel").ctx("context2").lastUpdateTime(sdf.parse("2017-03-20 12:12:35.111"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e3 = _e(ctx.nextLogicalId()).cat("opel").ctx("context3").lastUpdateUser("Kobi Shaul")
                .deleteTime(sdf.parse("2018-02-09 02:02:02.222"));
        e4 = _e(e3.logicalId).cat("mazda").ctx("context1").creationUser("Dudi Frid")
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        // Evalue entities for tests
        v1 = _v(ctx.nextValueId()).field("Car sale").value("Chevrolet").bdt("identifier").ctx("sale")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        v2 = _v(ctx.nextValueId()).field("garage").value("Zion with his sons").bdt("identifier").ctx("fixing cars")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu Peretz").creationTime(new Date(System.currentTimeMillis()));
        v3 = _v(ctx.nextValueId()).field("Car sales").value("chevrolet").bdt("California").ctx("Sale cars")
                .creationUser("Kobi Peretz").lastUpdateUser("Dudu Shaul").creationTime(new Date(System.currentTimeMillis()));
        v4 = _v(ctx.nextValueId()).field("Garage").value(322).bdt("Netanya").ctx("fixing cars").creationUser("Haim Melamed")
                .lastUpdateUser("haim Melamed").creationTime(sdf.parse("2018-04-17 13:05:13.098"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987"));
        v5 = _v(ctx.nextValueId()).field("Color").value("White & wtf Black ").bdt("Identifier").ctx("colors")
                .creationUser("Haim Melamed").creationTime(sdf.parse("2016-09-02 19:45:23.123"))
                .lastUpdateUser("haim Melamed").deleteTime(sdf.parse("2017-12-12 01:00:00.000"));
        v6 = _v(ctx.nextValueId()).field("date meeting").value(sdf.parse("2015-02-03 14:04:33.125")).bdt("Car owners meeting")
                .ctx("Replacing between people").creationUser("Chip of cars").lastUpdateUser("Yachial Nadav")
                .creationTime(sdf.parse("2016-09-02 19:45:23.123")).deleteTime(sdf.parse("2017-12-12 01:00:00.000"));
        v7 = _v(ctx.nextValueId()).field("North Garages").value(222).bdt("North").ctx("North country")
                .creationUser("Gabi Levy").lastUpdateUser("Gabi Levy").creationTime(sdf.parse("2014-08-18 18:08:18.888"))
                .lastUpdateTime(sdf.parse("2018-05-07 03:51:52.387"));
        v8 = _v(ctx.nextValueId()).field("North Garages").value("Black, what is you color ? white ?").bdt("Car owners meeting")
                .ctx("changing information").creationUser("Yaaaaaariv").lastUpdateUser("Yael Biniamin")
                .creationTime(sdf.parse("2017-07-07 17:47:27.727")).deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        v9 = _v(ctx.nextValueId()).field("North Garages").value(999).bdt("North").ctx("North country")
                .creationUser("Gabi Levy").lastUpdateUser("Gabi Levy").creationTime(sdf.parse("2014-08-18 18:08:18.888"))
                .lastUpdateTime(sdf.parse("2018-05-07 03:51:52.387"));
        v10 = _v(ctx.nextValueId()).field("conference date").value(sdf.parse("2013-01-01 11:01:31.121")).bdt("Car owners meeting")
                .ctx("changing information").creationUser("Yaaaaaariv").lastUpdateUser("Yael Biniamin")
                .creationTime(sdf.parse("2017-07-07 17:47:27.727")).deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        // Add Evalue to Entity
        e1.value(v1);
        e1.value(v2);
        e2.value(v3);
        e2.value(v4);
        e2.value(v5);
        e3.value(v6);
        e4.value(v7);
        e4.value(v8);
        e4.value(v9);
        e4.value(v10);

        // Insert Entity and Evalue entities to ES
        Assert.assertEquals(4, commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals(10, commit(ctx, INDEX, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
    }


    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testEqByEntityCategoryAndEvalueFieldId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new EProp(4, "fieldId", Constraint.of(ConstraintOp.eq, v1.fieldId))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(v1.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", v1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLogicalQueryEqByEntityCategoryAndEvalueFieldId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new EProp(4, "Car sale.stringValue", Constraint.of(ConstraintOp.eq, "Chevrolet"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(v1.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", v1.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testLikeByEntityCategoryLastUpdateUserAndOptionalEvalueFieldId_NotExist() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.like, "*pe*")),
                        new EProp(4, "lastUpdateUser", Constraint.of(ConstraintOp.eq, e1.lastUpdateUser)),
                        new EProp(5, "garage.stringValue", Constraint.of(ConstraintOp.eq, "Zion with his sons"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntity(v2.toEntity())
                        .withRelationships(e1.withRelations("hasEvalue", v2.id()))
                        .build())
                .build();

        // Check if expected results and actual results are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

}

