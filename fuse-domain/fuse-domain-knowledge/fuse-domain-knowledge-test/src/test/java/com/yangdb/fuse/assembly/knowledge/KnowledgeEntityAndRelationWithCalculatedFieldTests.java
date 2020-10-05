package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.aggregation.AggLOp;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.CalculatedEProp;
import com.yangdb.fuse.model.query.properties.projection.CalculatedFieldProjection;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.*;
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
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


public class KnowledgeEntityAndRelationWithCalculatedFieldTests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
    static ValueBuilder v1, v2, v3, v4, v5, v6, v7, v8, v9, v10;
    static RelationBuilder rel1, rel2, rel3, rel4, rel5;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(true,true); //todo remove remark when running IT tests
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

        // Evalue entities for tests
        v1 = _v(ctx.nextValueId()).field("Car sale").value("Chevrolet").bdt("identifier").ctx("sale")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        v2 = _v(ctx.nextValueId()).field("garage").value("Zion and his sons").bdt("identifier").ctx("fixing cars")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu Peretz").creationTime(new Date(System.currentTimeMillis()));
        v3 = _v(ctx.nextValueId()).field("Car sales").value("chevrolet").bdt("California").ctx("Sale cars")
                .creationUser("Kobi Peretz").lastUpdateUser("Dudu Shaul").creationTime(new Date(System.currentTimeMillis()));
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
        v8 = _v(ctx.nextValueId()).field("North Garages").value(sdf.parse("2013-01-01 11:01:31.121")).bdt("Car owners meeting")
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

        rel2.sideA(e1).sideB(e3);
        e1.rel(rel2, "out");
        e3.rel(rel2, "in");

        rel3.sideA(e1).sideB(e4);
        e1.rel(rel3, "out");
        e4.rel(rel3, "in");

        // Insert Entity and Reference entities to ES
        Assert.assertEquals("error loading data ",8, commit(ctx, INDEX, e1, e2, e3));
        Assert.assertEquals("error loading data ",3, commit(ctx, REL_INDEX, rel1, rel2, rel3));
        Assert.assertEquals("error loading data ",10, commit(ctx, INDEX, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
    }

    @AfterClass
    public static void after() {
        if(ctx!=null) Assert.assertEquals(21,ctx.removeCreated());

    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testQueryEntityRelationWithCalcFieldCountRel() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList( 4, 7), 0),
                        new CalculatedEProp(7, "R", new CalculatedFieldProjection(AggLOp.count)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R", "Relation", 0, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        Assert.assertEquals(pageData.getSize(),1);
        Assert.assertEquals(((AssignmentsQueryResult) pageData).getAssignments().size(),1);
        Assert.assertEquals(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities().size(),6);

        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e1.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e2.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e3.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()

                .stream().filter(e->e.geteID().equals(rel1.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(rel2.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(rel3.id())).findAny().isPresent());

        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e1.id())).findAny()
                .get().getProperty("count[R]").get().getValue().equals(3));

        Assert.assertEquals(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getRelationships().size(),5);

    }

    @Test
    public void testQueryEntityEvalueAndRelationWithCalcFieldCountRel() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList( 4, 6, 8, 9), 0),
                          new CalculatedEProp(8, "R", new CalculatedFieldProjection(AggLOp.count)),
                             new Rel(4, "hasRelation", R, null, 5, 0),
                             new ETyped(5, "R", "Relation", 0, 0),
                          new CalculatedEProp(9, "V", new CalculatedFieldProjection(AggLOp.count)),
                             new Rel(6, "hasEvalue", R, null, 7, 0),
                             new ETyped(7, "V", "Evalue", 0, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        Assert.assertEquals(pageData.getSize(),1);
        Assert.assertEquals(((AssignmentsQueryResult) pageData).getAssignments().size(),1);
        Assert.assertEquals(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities().size(),12);

        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e1.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e2.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e3.id())).findAny().isPresent());

        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(v1.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(v2.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(v3.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(v4.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(v5.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(v6.id())).findAny().isPresent());

        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(rel1.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(rel2.id())).findAny().isPresent());
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(rel3.id())).findAny().isPresent());

        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e1.id())).findAny()
                .get().getProperty("count[R]").get().getValue().equals(3));
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e1.id())).findAny()
                .get().getProperty("count[V]").get().getValue().equals(2));

        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e2.id())).findAny()
                .get().getProperty("count[R]").get().getValue().equals(1));
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities()
                .stream().filter(e->e.geteID().equals(e2.id())).findAny()
                .get().getProperty("count[V]").get().getValue().equals(3));


        Assert.assertEquals(((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getRelationships().size(),11);

    }

}
