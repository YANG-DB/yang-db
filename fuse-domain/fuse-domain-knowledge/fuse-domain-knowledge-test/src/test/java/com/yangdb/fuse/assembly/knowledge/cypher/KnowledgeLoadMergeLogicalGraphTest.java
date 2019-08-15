package com.yangdb.fuse.assembly.knowledge.cypher;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.relation.Relation;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.nextPage;
import static com.yangdb.fuse.client.FuseClient.countGraphElements;

/**
 * http://web.madstudio.northwestern.edu/re-visualizing-the-novel/
 * http://studentwork.prattsi.org/infovis/visualization/les-miserables-character-network-visualization/
 */
public class KnowledgeLoadMergeLogicalGraphTest {

    //number of elements on les miserables graph

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(true);
        //load data
        loadData();
    }

    private static void loadData() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("./data/logical/les_miserables.json");
        QueryResourceInfo info = fuseClient.loadData(KNOWLEDGE, resource);
        Assert.assertNotNull(info);
    }


    @Test
    public void testFetchEntityWithRelationGraph() throws IOException, InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("./data/logical/les_miserables_append.json");
        QueryResourceInfo info = fuseClient.upsertData(KNOWLEDGE, resource);
        Assert.assertNotNull(info);

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) where e.techId = 'Napoleon' AND ev.stringValue IN ['Napoleon_1','Napoleon'] Return *";
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        //validate merge did add new property 'Napoleon_1' without removing the former propery 'Napoleon'
        List<Entity> entities = ((AssignmentsQueryResult<Entity, Relation>) pageData).getAssignments().get(0).getEntities();
        Assert.assertEquals(1,entities.stream().filter(e->e.geteType().equals("Entity"))
                .filter(e->e.getProperty("techId").get().getValue().toString().equals("Napoleon")).count());
        Assert.assertEquals(1,entities.stream().filter(e->e.geteType().equals("Evalue"))
                .filter(e->e.getProperty("stringValue").get().getValue().toString().equals("Napoleon")).count());
        Assert.assertEquals(1,entities.stream().filter(e->e.geteType().equals("Evalue"))
                .filter(e->e.getProperty("stringValue").get().getValue().toString().equals("Napoleon_1")).count());

        query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) where e.techId = 'Myriel' AND ev.stringValue in ['Myriel','female'] Return *";
        // get Query URL
        queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        //validate merge did add new property 'Napoleon_1' without removing the former propery 'Napoleon'
        entities = ((AssignmentsQueryResult<Entity, Relation>) pageData).getAssignments().get(0).getEntities();
        Assert.assertEquals(1,entities.stream().filter(e->e.geteType().equals("Entity"))
                .filter(e->e.getProperty("techId").get().getValue().toString().equals("Myriel")).count());
        Assert.assertEquals(1,entities.stream().filter(e->e.geteType().equals("Evalue"))
                .filter(e->e.getProperty("fieldId").get().getValue().toString().equals("name")).count());
        Assert.assertEquals(1,entities.stream().filter(e->e.geteType().equals("Evalue"))
                .filter(e->e.getProperty("fieldId").get().getValue().toString().equals("sex")).count());


        query = "Match (e:Entity)-[r:relatedEntity]->(eOut:Entity) where e.techId = 'hezi' Return *";
        // get Query URL
        queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        //validate merge did add new property 'Napoleon_1' without removing the former propery 'Napoleon'
        List<Relation> relations = ((AssignmentsQueryResult<Entity, Relation>) pageData).getAssignments().get(0).getRelationships();
        Assert.assertEquals(2,relations.size());

        query = "Match (e:Entity)-[r:relatedEntity]->(eOut:Entity) where eOut.techId = 'moti' Return *";
        // get Query URL
        queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        //validate merge did add new property 'Napoleon_1' without removing the former propery 'Napoleon'
        relations = ((AssignmentsQueryResult<Entity, Relation>) pageData).getAssignments().get(0).getRelationships();
        Assert.assertEquals(2,relations.size());
    }


}
