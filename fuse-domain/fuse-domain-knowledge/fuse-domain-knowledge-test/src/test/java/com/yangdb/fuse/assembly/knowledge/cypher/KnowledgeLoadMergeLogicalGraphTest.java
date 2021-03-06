package com.yangdb.fuse.assembly.knowledge.cypher;

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.client.FuseClientSupport.nextPage;

/**
 * http://web.madstudio.northwestern.edu/re-visualizing-the-novel/
 * http://studentwork.prattsi.org/infovis/visualization/les-miserables-character-network-visualization/
 */
public class KnowledgeLoadMergeLogicalGraphTest {

    //number of elements on les miserables graph

    static SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);

    @BeforeClass
    public static void setup() throws Exception {
       // Setup.setup(true); //todo remove remark when running IT tests
       // loadData();       //todo remove remark when running IT tests
    }

    private static void loadData() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("./data/logical/les_miserables.json");
        ResultResourceInfo info = fuseClient.loadGraphData(KNOWLEDGE, resource);
        Assert.assertNotNull(info);
    }


    @Test
    public void testFetchEntityWithRelationGraph() throws IOException, InterruptedException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("./data/logical/les_miserables_append.json");
        ResultResourceInfo info = fuseClient.upsertGraphData(KNOWLEDGE, resource);
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
        List<Entity> entities = ((AssignmentsQueryResult<Entity, com.yangdb.fuse.model.results.Relationship>) pageData).getAssignments().get(0).getEntities();
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
        entities = ((AssignmentsQueryResult<Entity, com.yangdb.fuse.model.results.Relationship>) pageData).getAssignments().get(0).getEntities();
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
        List<com.yangdb.fuse.model.results.Relationship> relations = ((AssignmentsQueryResult<Entity, com.yangdb.fuse.model.results.Relationship>) pageData).getAssignments().get(0).getRelationships();
        Assert.assertEquals(2,relations.size());

        query = "Match (e:Entity)-[r:relatedEntity]->(eOut:Entity) where eOut.techId = 'moti' Return *";
        // get Query URL
        queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        //validate merge did add new property 'Napoleon_1' without removing the former propery 'Napoleon'
        relations = ((AssignmentsQueryResult<Entity, com.yangdb.fuse.model.results.Relationship>) pageData).getAssignments().get(0).getRelationships();
        Assert.assertEquals(2,relations.size());
    }


}
