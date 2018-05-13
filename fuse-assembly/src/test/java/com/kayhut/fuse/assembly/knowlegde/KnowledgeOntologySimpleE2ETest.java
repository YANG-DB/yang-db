package com.kayhut.fuse.assembly.knowlegde;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.GlobalElasticEmbeddedNode;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by rani on 5/2/2018.
 */
public class KnowledgeOntologySimpleE2ETest {
    static KnowledgeDataInfraManager manager;
    static FuseClient fuseClient;
    @BeforeClass
    public static void setup() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String val = classLoader.getResource("assembly/Knowledge/config/application.test.engine3.m1.dfs.knowledge.public.conf").getFile();
        manager = new KnowledgeDataInfraManager(val);
        manager.client_connect();
        manager.init();
        manager.load();
        manager.client_close();

        System.out.println("KnowledgeOntology runner started");
        start = System.currentTimeMillis();

        //elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();

        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()), "activeProfile"); //, "m1.dfs.non_redundant");

        app.start("server.join=false");

        fuseClient = new FuseClient("http://localhost:8888/fuse");
    }

    private AssignmentsQueryResult GetAssignmentForQuery(Query query, FuseResourceInfo resourceInfo, int timeout, int sleeptime) throws IOException, InterruptedException
    {
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(resourceInfo.getQueryStoreUrl(), query);
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        cursorRequest.setTimeout(timeout);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(sleeptime);
            }
        }

        return (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }

    @Test
    public void ComplexQueryTest() throws IOException, InterruptedException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("ComplexQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEntityReference"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Reference"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11), 0),
                new EProp(10, $ont.pType$("url"), Constraint.of(ConstraintOp.like, "*clown")),
                new EProp(11, $ont.pType$("title"), Constraint.of(ConstraintOp.notEmpty)/*, "sample")*/))
        ).build();

        AssignmentsQueryResult pageData = GetAssignmentForQuery(query, fuseResourceInfo, 5000, 10);
        int resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        String rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,2);
            Assert.assertEquals(relationsCount,1);
        }

        /*Query query = Query.Builder.instance().withName("ComplexQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*k1")),
                new EProp(11, $ont.pType$("bdt"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();*/

        query = Query.Builder.instance().withName("ComplexQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "Nic*")),
                new EProp(11, $ont.pType$("creationTime"), Constraint.of(ConstraintOp.gt, "2018-01-01 00:00:00.000")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();

        /*
        query = Query.Builder.instance().withName("SimpleQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "Nic*")),
                new EProp(11, $ont.pType$("bdt"), Constraint.of(ConstraintOp.match, "nicknames")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();
         */

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 5000, 10);
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            Assert.assertNull(pageData.getAssignments().get(i).getEntities());
            Assert.assertNull(pageData.getAssignments().get(i).getRelationships());
            /*int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,2);
            Assert.assertEquals(relationsCount,1);*/
        }

        query = Query.Builder.instance().withName("ComplexQuery2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.some, Arrays.asList( 6,7,13), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "Nic*")),
                new EProp(11, $ont.pType$("creationTime"), Constraint.of(ConstraintOp.gt, "2018-01-01 00:00:00.000")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(13, $ont.rType$("hasEntityReference"), R, null, 14, 0),
                new ETyped(14, "B", $ont.eType$("Reference"), 15, 0),
                new Quant1(15, QuantType.all, Arrays.asList(16, 17), 0),
                new EProp(16, $ont.pType$("url"), Constraint.of(ConstraintOp.like, "*circus")),
                new EProp(17, $ont.pType$("creationTime"), Constraint.of(ConstraintOp.gt, "2018-01-01 00:00:00.000"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 5000, 10);
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,2);
            Assert.assertEquals(relationsCount,1);
        }
    }

    @Test
    public void SimpleQueryTest() throws IOException, InterruptedException {
        //FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        // Fit the query to the sample data
        /*Query query = Query.Builder.instance().withName("SimpleQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("LogicalEntity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(101, 3), 0),
                new EProp(101, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(3, $ont.rType$("hasEntity"), R, null, 4, 0),
                new ETyped(4, "B", $ont.eType$("Entity"), 5, 0),
                new Quant1(5, QuantType.all, Arrays.asList(6), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context2")))
        ).build();*/

        Query query = Query.Builder.instance().withName("SimpleQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Nick1")),
                new EProp(11, $ont.pType$("bdt"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();

        /*QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        cursorRequest.setTimeout(60000);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());*/
        AssignmentsQueryResult pageData = GetAssignmentForQuery(query, fuseResourceInfo, 5000, 10);
        int resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        String rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,2);
            Assert.assertEquals(relationsCount,1);
        }

        query = Query.Builder.instance().withName("SimpleQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList( 11, 12), 0),
                new EProp(11, $ont.pType$("bdt"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();

        /*QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        cursorRequest.setTimeout(60000);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());*/
        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 5000, 10);
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,4);
            Assert.assertEquals(relationsCount,2);
        }

        //CreateGraphHierarchyCursorRequest hcursorRequest = new CreateGraphHierarchyCursorRequest(5000);
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CreatePathsCursorRequest pCursorRequest = new CreatePathsCursorRequest(5000);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), pCursorRequest);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,2);
        rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,2);
            Assert.assertEquals(relationsCount,1);
        }

        query = Query.Builder.instance().withName("SimpleQuery2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEntityReference"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Reference"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList( 11, 12), 0),
                new EProp(11, $ont.pType$("system"), Constraint.of(ConstraintOp.eq, "system7")),
                new EProp(12, $ont.pType$("creationUser"), Constraint.of(ConstraintOp.eq, "Test2"))
        )).build();

        queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        cursorRequest.setTimeout(60000);
        cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,2);
            Assert.assertEquals(relationsCount,1);
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        //cleanup(CsvCursorTestSuite.elasticEmbeddedNode.getClient());

        if (app != null) {
            app.stop();
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("KnowledgeOntology runner finished. elapsed: " + elapsed);
        manager.client_connect();
        manager.drop();
        manager.client_close();
    }

    private static long start;
    private static Jooby app;
    public static ElasticEmbeddedNode elasticEmbeddedNode;
}
