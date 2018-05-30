package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowlegde.KnowledgeDatasetLoader;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import org.jooby.Jooby;
import org.junit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by user pc on 5/18/2018.
 */
@Ignore
public class KnowledgeOntologyFilmIMDBTests {
    static KnowledgeDatasetLoader loader;
    private static Jooby app;
    static FuseClient fuseClient;

    @BeforeClass
    public static void setup() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String val = classLoader.getResource("assembly/Knowledge/config/application.test.engine3.m1.dfs.knowledge.public.conf").getFile();
        loader = new KnowledgeDatasetLoader(val);

        /*app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()), "activeProfile"); //, "m1.dfs.non_redundant");

        app.start("server.join=false");

        fuseClient = new FuseClient("http://localhost:8888/fuse");*/
    }

    private AssignmentsQueryResult GetAssignmentForQuery(Query query, FuseResourceInfo resourceInfo, int timeout, int sleeptime, int cursorType) throws IOException, InterruptedException
    {
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(resourceInfo.getQueryStoreUrl(), query);
        CreateCursorRequest cursorRequest = null;

        switch (cursorType) {
            case 0:
                cursorRequest = new CreateGraphCursorRequest();
                break;
            case 1:
                cursorRequest = new CreatePathsCursorRequest();
                break;
            case 2:
                cursorRequest = new CreateGraphHierarchyCursorRequest();
                break;
            default:
                cursorRequest = new CreateGraphCursorRequest();
                break;
        }
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
    public void IMDBLoad() throws FileNotFoundException, IOException, ParseException {
        loader.client_connect();
        loader.init();
        loader.loadFromIMDBJson(20);
        loader.indexImdbJsons();
        loader.client_close();
    }

    @Test
    public void IMDBTests() throws FileNotFoundException, IOException, ParseException {

    }

    @Test
    public void IMDBSomeQueries() throws IOException, InterruptedException {
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "resources", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()), "activeProfile"); //, "m1.dfs.non_redundant");

        app.start("server.join=false");

        fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("IMDBRelationQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 7,8), 0),
                //new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasRelation"), R, null, 13, 0),
                new Rel(8, $ont.rType$("hasEvalue"), R, null, 12, 0),
                new ETyped(12, "B", $ont.eType$("Evalue"), 14, 0),
                new ETyped(13, "C", $ont.eType$("Relation"), 17, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15,16), 0),
                new EProp(15, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "language")),
                new EProp(16, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Mandarin")),
                new Quant1(17, QuantType.all, Arrays.asList(18), 0),
                new Rel(18, $ont.rType$("hasRelation"), L, null, 19, 0),
                new ETyped(19, "E", $ont.eType$("Entity"), 20, 0),
                new Quant1(20, QuantType.all, Arrays.asList( 22), 0),
                new Rel(22, $ont.rType$("hasEvalue"), R, null, 23, 0),
                new ETyped(23, "D", $ont.eType$("Evalue"), 24, 0),
                new Quant1(24, QuantType.all, Arrays.asList(25,26), 0),
                new EProp(25, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "director")),
                new EProp(26, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Jiang Xiao"))
                )
        ).build();

        AssignmentsQueryResult pageData = GetAssignmentForQuery(query, fuseResourceInfo, 50000, 10,0);
        int resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        String rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,5);
            Assert.assertEquals(relationsCount,4);
        }

        query = Query.Builder.instance().withName("IMDBDirectorQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,8), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(8, $ont.rType$("hasEvalue"), R, null, 13, 0),
                new ETyped(13, "C", $ont.eType$("Evalue"), 14, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15,16), 0),
                new EProp(15, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "director")),
                new EProp(16, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Francis Searle"))
                )
        ).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 50000, 10,0);
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize,1);
        rtype = pageData.getResultType();
        for(int i=0;i<resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount,2);
            Assert.assertEquals(relationsCount,1);
        }

        /*Query
                query = Query.Builder.instance().withName("IMDBQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7,8), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasInsight"), R, null, 9, 0),
                new Rel(8, $ont.rType$("hasEvalue"), R, null, 13, 0),
                new ETyped(9, "B", $ont.eType$("Insight"), 10, 0),
                new Quant1(10, QuantType.all, Arrays.asList(11, 12), 0),
                new EProp(11, $ont.pType$("content"), Constraint.of(ConstraintOp.eq, "4 wins & 1 nomination." // "*wins*")),
        new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new ETyped(13, "C", $ont.eType$("Evalue"), 14, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15,16), 0),
                new EProp(15, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(15, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Electric Shadows"))
                )
        ).build();
         */

        /*Query query = Query.Builder.instance().withName("IMDBQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,8), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(8, $ont.rType$("hasEvalue"), R, null, 13, 0),
                new ETyped(13, "C", $ont.eType$("Evalue"), 14, 0),
                new Quant1(14, QuantType.some, Arrays.asList(15,16), 0),
                //new EProp(15, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                //new EProp(16, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Electric Shadows"))
                new Quant1(15, QuantType.all, Arrays.asList(17,18), 0),
                new Quant1(16, QuantType.all, Arrays.asList(19,20), 0),
                new EProp(17, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "runtime")),
                new EProp(18, $ont.pType$("intValue"), Constraint.of(ConstraintOp.eq, 93)),
                new EProp(19, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "year")),
                new EProp(20, $ont.pType$("intValue"), Constraint.of(ConstraintOp.eq, 2004))
                )
        ).build();*/

        /*Query query = Query.Builder.instance().withName("IMDBQueryWithEPropGroup").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,8), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(8, $ont.rType$("hasEvalue"), R, null, 13, 0),
                new ETyped(13, "C", $ont.eType$("Evalue"), 14, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15), 0),
                new EPropGroup(15,QuantType.some, Arrays.asList(), Arrays.asList(
                        new EPropGroup(16, QuantType.all, Arrays.asList(
                                new EProp(17, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "runtime")),
                                new EProp(18, $ont.pType$("intValue"), Constraint.of(ConstraintOp.eq, 93)))),
                        new EPropGroup(22,QuantType.all, Arrays.asList(
                                new EProp(19, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "year")),
                                new EProp(20, $ont.pType$("intValue"), Constraint.of(ConstraintOp.eq, 2004))))))
                )
        ).build();*/

        /*Query query = Query.Builder.instance().withName("IMDBQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 7,8), 0),
                //new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 13, 0),
                new Rel(8, $ont.rType$("hasEvalue"), R, null, 12, 0),
                new ETyped(12, "B", $ont.eType$("Evalue"), 14, 0),
                new ETyped(13, "C", $ont.eType$("Evalue"), 17, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15,16), 0),
                //new EProp(15, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                //new EProp(16, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Electric Shadows"))
                //new Quant1(15, QuantType.all, Arrays.asList(17,18), 0),
                //new Quant1(16, QuantType.all, Arrays.asList(19,20), 0),
                //new EProp(17, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "runtime")),
                //new EProp(18, $ont.pType$("intValue"), Constraint.of(ConstraintOp.eq, 93)),
                new EProp(15, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "language")),
                new EProp(16, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Mandarin")),
                new Quant1(17, QuantType.all, Arrays.asList(18,19), 0),
                new EProp(18, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "year")),
                new EProp(19, $ont.pType$("intValue"), Constraint.of(ConstraintOp.eq, 2004))
                )
        ).build();*/




       /*Query query = Query.Builder.instance().withName("IMDBQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList( 6,7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasInsight"), R, null, 9, 0),
                //new Rel(8, $ont.rType$("hasEvalue"), R, null, 13, 0),
                new ETyped(9, "B", $ont.eType$("Insight"), 10, 0),
                new Quant1(10, QuantType.all, Arrays.asList(11, 12), 0),
                new EProp(11, $ont.pType$("title"), Constraint.of(ConstraintOp.like, "Title")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new ETyped(13, "C", $ont.eType$("Evalue"), 14, 0),
                new Quant1(14, QuantType.all, Arrays.asList(15), 0),
                new EProp(15, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title"))
                )
        ).build();*/
    }

    @AfterClass
    public static void cleanup() throws Exception {
        /*loader.client_connect();
        loader.drop();
        loader.client_close();*/

        if (app != null) {
            app.stop();
        }
    }
}