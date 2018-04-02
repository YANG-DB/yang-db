package com.kayhut.fuse.services.engine2;

import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.ontology.Ontology;
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
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

public class RealClusterKnowledgeRuleBasedSearchTest {

    @Test
    @Ignore
    public void testAdvancedSearch() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 9, 14, 28), 0),
                new EProp(3, "context", Constraint.of(ConstraintOp.eq, "globAL")),
                new Rel(4, $ont.rType$("hasEvalue"), R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Evalue"), 6, 0),
                new Quant1(6, QuantType.all, Arrays.asList(7, 8, 29), 0),
                new EProp(7, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(8, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),
                new Rel(9, $ont.rType$("hasEvalue"), R, null, 10, 0),
                new ETyped(10, "B", $ont.eType$("Evalue"), 11, 0),
                new Quant1(11, QuantType.all, Arrays.asList(12, 13, 30), 0),
                new EProp(12, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(13, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "***")),
                new Rel(14, $ont.rType$("hasEntity"), L, null, 15, 0),
                new ETyped(15, "B", $ont.eType$("LogicalEntity"), 16, 0),
                new Rel(16, $ont.rType$("hasEntity"), R, null, 17, 0),
                new ETyped(17, "B", $ont.eType$("Entity"), 18, 0),
                new Quant1(18, QuantType.all, Arrays.asList(19, 20, 21, 22, 31), 0),
                new EProp(19, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "global")),
                new EProp(20, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(21, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "balla")),
                new OptionalComp(22, 23),
                new Rel(23, $ont.rType$("hasEvalue"), R, null, 24, 0),
                new ETyped(24, "B", $ont.eType$("Evalue"), 25, 0),
                new Quant1(25, QuantType.all, Arrays.asList(26, 27, 32), 0),
                new EProp(26, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "description")),
                new EProp(27, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),

                new EProp(28, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(29, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(30, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(31, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(32, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        Assert.assertNotNull(actualPlan);
        Assert.assertTrue(((EntityOp) actualPlan.getOps().get(0)).getAsgEbase().geteNum() == 17);

    }

    @Test
    @Ignore
    public void testAdvancedSearchFieldId() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 9, 14, 28), 0),
                new EProp(3, "context", Constraint.of(ConstraintOp.eq, "globAL")),
                new Rel(4, $ont.rType$("hasEvalue"), R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Evalue"), 6, 0),
                new Quant1(6, QuantType.all, Arrays.asList(7, 8, 29), 0),
                new EProp(7, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(8, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "a*")),
                new Rel(9, $ont.rType$("hasEvalue"), R, null, 10, 0),
                new ETyped(10, "B", $ont.eType$("Evalue"), 11, 0),
                new Quant1(11, QuantType.all, Arrays.asList(12, 13, 30), 0),
                new EProp(12, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(13, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "***")),
                new Rel(14, $ont.rType$("hasEntity"), L, null, 15, 0),
                new ETyped(15, "B", $ont.eType$("LogicalEntity"), 16, 0),
                new Rel(16, $ont.rType$("hasEntity"), R, null, 17, 0),
                new ETyped(17, "B", $ont.eType$("Entity"), 18, 0),
                new Quant1(18, QuantType.all, Arrays.asList(19, 20, 21, 22, 31), 0),
                new EProp(19, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "global")),
                new EProp(20, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(21, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "balla")),
                new OptionalComp(22, 23),
                new Rel(23, $ont.rType$("hasEvalue"), R, null, 24, 0),
                new ETyped(24, "B", $ont.eType$("Evalue"), 25, 0),
                new Quant1(25, QuantType.all, Arrays.asList(26, 27, 32), 0),
                new EProp(26, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "description")),
                new EProp(27, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),

                new EProp(28, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(29, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(30, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(31, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(32, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        Assert.assertNotNull(actualPlan);
        Assert.assertTrue(((EntityOp) actualPlan.getOps().get(0)).getAsgEbase().geteNum() == 5);
    }

    @Test
    @Ignore
    public void testAdvancedSearchFieldIdWithAsteriskOnly() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 9, 14, 28), 0),
                new EProp(3, "context", Constraint.of(ConstraintOp.eq, "globAL")),
                new Rel(4, $ont.rType$("hasEvalue"), R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Evalue"), 6, 0),
                new Quant1(6, QuantType.all, Arrays.asList(7, 8, 29), 0),
                new EProp(7, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(8, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),
                new Rel(9, $ont.rType$("hasEvalue"), R, null, 10, 0),
                new ETyped(10, "B", $ont.eType$("Evalue"), 11, 0),
                new Quant1(11, QuantType.all, Arrays.asList(12, 13, 30), 0),
                new EProp(12, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(13, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "***")),
                new Rel(14, $ont.rType$("hasEntity"), L, null, 15, 0),
                new ETyped(15, "B", $ont.eType$("LogicalEntity"), 16, 0),
                new Rel(16, $ont.rType$("hasEntity"), R, null, 17, 0),
                new ETyped(17, "B", $ont.eType$("Entity"), 18, 0),
                new Quant1(18, QuantType.all, Arrays.asList(19, 20, 21, 22, 31), 0),
                new EProp(19, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "global")),
                new EProp(20, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(21, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "balla")),
                new OptionalComp(22, 23),
                new Rel(23, $ont.rType$("hasEvalue"), R, null, 24, 0),
                new ETyped(24, "B", $ont.eType$("Evalue"), 25, 0),
                new Quant1(25, QuantType.all, Arrays.asList(26, 27, 32), 0),
                new EProp(26, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "description")),
                new EProp(27, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),

                new EProp(28, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(29, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(30, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(31, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(32, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        Assert.assertNotNull(actualPlan);
        Assert.assertTrue(((EntityOp) actualPlan.getOps().get(0)).getAsgEbase().geteNum() == 17);
    }

    @Test
    @Ignore
    public void testBasicSearchNoContext() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Evalue"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 5, 40, 42), 0),
                new EProp(3, "fieldId", Constraint.of(ConstraintOp.inSet, Arrays.asList("title", "nicknames"))),
                new EProp(40, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new Rel(5, $ont.rType$("hasEvalue"), L, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Entity"), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9, 12, 41), 0),
                new EProp(8, "context", Constraint.of(ConstraintOp.eq, "globAL")),
                new Rel(9, $ont.rType$("hasEvalue"), R, null, 10, 0),
                new ETyped(10, "B", $ont.eType$("Evalue"), 11, 0),
                new EProp(11, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.inSet, Arrays.asList("description", "nicknames"))),
                new Rel(12, $ont.rType$("hasEntity"), L, null, 13, 0),
                new ETyped(13, "B", $ont.eType$("LogicalEntity"), 14, 0),
                new Rel(14, $ont.rType$("hasEntity"), R, null, 15, 0),
                new ETyped(15, "B", $ont.eType$("Entity"), 16, 0),
                new EProp(16, $ont.pType$("context"), Constraint.of(ConstraintOp.ne, "global")),
                new EProp(41, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(42, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "Shirle*"))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        Assert.assertNotNull(actualPlan);
        Assert.assertTrue(((EntityOp) actualPlan.getOps().get(0)).getAsgEbase().geteNum() == 1);
    }

    @Test
    @Ignore
    public void testBasicSearchWithContext() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Evalue"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 5, 40, 42), 0),
                new EProp(3, "fieldId", Constraint.of(ConstraintOp.inSet, Arrays.asList("title", "nicknames"))),
                new EProp(40, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new Rel(5, $ont.rType$("hasEvalue"), L, null, 6, 0),
                new ETyped(6, "B", $ont.eType$("Entity"), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 12), 0),
                new EProp(8, "context", Constraint.of(ConstraintOp.eq, "globAL")),
                new Rel(12, $ont.rType$("hasEntity"), L, null, 13, 0),
                new ETyped(13, "B", $ont.eType$("LogicalEntity"), 14, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15, 19), 0),
                new Rel(15, $ont.rType$("hasEntity"), R, null, 16, 0),
                new ETyped(16, "B", $ont.eType$("Entity"), 17, 0),
                new Quant1(17, QuantType.all, Arrays.asList(18, 41), 0),
                new EProp(18, "context", Constraint.of(ConstraintOp.eq, "cont1")),
                new Rel(19, $ont.rType$("hasEntity"), R, null, 20, 0),
                new ETyped(20, "B", $ont.eType$("Entity"), 21, 0),
                new Quant1(21, QuantType.all, Arrays.asList(22, 23), 0),
                new EProp(22, "context", Constraint.of(ConstraintOp.inSet, Arrays.asList("global", "cont1"))),
                new OptionalComp(23, 24),
                new Rel(24, $ont.rType$("hasEvalue"), R, null, 25, 0),
                new ETyped(25, "B", $ont.eType$("Evalue"), 26, 0),
                new Quant1(26, QuantType.all, Arrays.asList(27), 0),
                new EProp(27, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.inSet, Arrays.asList("title", "desc", "nicknames"))),
                new EProp(41, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(42, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "Shirle*"))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        Assert.assertNotNull(actualPlan);
        Assert.assertTrue(((EntityOp) actualPlan.getOps().get(0)).getAsgEbase().geteNum() == 1);
    }
}
