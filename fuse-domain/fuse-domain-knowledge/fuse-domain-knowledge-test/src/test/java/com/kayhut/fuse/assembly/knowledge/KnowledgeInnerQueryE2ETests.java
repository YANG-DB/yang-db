package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


public class KnowledgeInnerQueryE2ETests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4;
    static ValueBuilder v1, v2, v3, v4, v5, v6, v7, v8, v9, v10;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception
    {
        Setup.setup(false,true);
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

        // Insert Entity and Evalue entities to ES
        Assert.assertEquals(4, commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals(10, commit(ctx, INDEX, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
    }


    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }


    // Start Tests:
    @Test
    public void testSimpleInnerQueryWithConstraintOnId() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryInner = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, e1.logicalId))
                )).build();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "V", "Evalue", 6, 0),
                        new EProp(6, "id", InnerQueryConstraint.of(ConstraintOp.inSet,queryInner,"A","id"))
                )).build();


        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));


        Assert.assertEquals("call[q1]",graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1,((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(3, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());

        QueryResourceInfo pathResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreatePathsCursorRequest(new CreatePageRequest(100))));

        Assert.assertEquals("call[q1]",pathResourceInfo.getResourceId());
        Assert.assertNotNull(pathResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(2,((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(1)).get("entities")).size());

    }

    @Test
    public void testSimpleInnerQueryWithConstraintOnField() throws IOException, InterruptedException
    {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryInner = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "fieldId", Constraint.of(ConstraintOp.contains, "Garage"))
                )).build();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "V", "Evalue", 6, 0),
                        new EProp(6, "id", InnerQueryConstraint.of(ConstraintOp.inSet,queryInner,"A","id"))
                )).build();


        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q2", "q2", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));


        Assert.assertEquals("call[q2]",graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1,((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(3, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());

        QueryResourceInfo pathResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q2", "q2", queryOuter, new CreatePathsCursorRequest(new CreatePageRequest(100))));

        Assert.assertEquals("call[q2]",pathResourceInfo.getResourceId());
        Assert.assertNotNull(pathResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(2,((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(1)).get("entities")).size());

    }


}
