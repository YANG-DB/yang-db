package com.yangdb.fuse.assembly.knowledge.cypher;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
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
public class KnowledgeUploadLogicalGraphTest {

    //number of elements on les miserables graph

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(true);
    }


    @Test
    public void testLoadFileGraph() throws IOException, InterruptedException, URISyntaxException {
        //load data
        URL resource = Thread.currentThread().getContextClassLoader().getResource("data/logical/les_miserables.json");
        QueryResourceInfo info = fuseClient.uploadFile(KNOWLEDGE, resource);
        Assert.assertNotNull(info);

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasRelation]->(rel:Relation) Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            Assert.assertEquals(1, pageData.getSize());
            Assert.assertEquals(1, assignments.size());
            Assert.assertEquals(false, assignments.get(0).getRelationships().isEmpty());
            Assert.assertEquals(false, assignments.get(0).getEntities().isEmpty());

            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        Assert.assertEquals(839, totalGraphSize);
    }

    @Test
    public void testLoadZippedFileGraph() throws IOException, InterruptedException, URISyntaxException {
        //load data
        URL resource = Thread.currentThread().getContextClassLoader().getResource("data/logical/les_miserables.json.gz");
        QueryResourceInfo info = fuseClient.uploadFile(KNOWLEDGE, resource);
        Assert.assertNotNull(info);

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasRelation]->(rel:Relation) Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            Assert.assertEquals(1, pageData.getSize());
            Assert.assertEquals(1, assignments.size());
            Assert.assertEquals(false, assignments.get(0).getRelationships().isEmpty());
            Assert.assertEquals(false, assignments.get(0).getEntities().isEmpty());

            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        Assert.assertEquals(839, totalGraphSize);
    }


}
