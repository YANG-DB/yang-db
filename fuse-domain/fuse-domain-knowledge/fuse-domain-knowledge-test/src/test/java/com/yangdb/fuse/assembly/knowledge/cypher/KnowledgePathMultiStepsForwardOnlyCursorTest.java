package com.yangdb.fuse.assembly.knowledge.cypher;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateForwardOnlyPathTraversalCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.countGraphElements;
import static com.yangdb.fuse.client.FuseClientSupport.nextPage;

public class KnowledgePathMultiStepsForwardOnlyCursorTest {

    //number of elements on les miserables graph

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(true);
//        loadData();
    }



    @Test
    public void testFetchEntityWithRelation4StepsLogicalResultPathSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                        " (e1:Entity)-[rv1:hasEvalue]->(ev1:Evalue {stringValue: 'Myriel'}), " +
                        " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                        " (e3:Entity)-[r3:relatedEntity]->(e4:Entity), " +
                        " (e4:Entity)-[r4:relatedEntity]->(e5:Entity) " +
                        " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(33, totalGraphSize);
    }
    @Test
    public void testFetchEntityWithRelation5StepsLogicalResultPathSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                        " (e1:Entity)-[rv1:hasEvalue]->(ev1:Evalue {stringValue: 'Myriel'}), " +
                        " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                        " (e3:Entity)-[r3:relatedEntity]->(e4:Entity), " +
                        " (e4:Entity)-[r4:relatedEntity]->(e5:Entity), " +
                        " (e5:Entity)-[r5:relatedEntity]->(e6:Entity) " +
                        " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(52, totalGraphSize);
    }
    @Test
    public void testFetchEntityWithRelation6StepsLogicalResultPathSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                        " (e1:Entity)-[rv1:hasEvalue]->(ev1:Evalue {stringValue: 'Myriel'}), " +
                        " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                        " (e3:Entity)-[r3:relatedEntity]->(e4:Entity), " +
                        " (e4:Entity)-[r4:relatedEntity]->(e5:Entity), " +
                        " (e5:Entity)-[r5:relatedEntity]->(e6:Entity), " +
                        " (e6:Entity)-[r6:relatedEntity]->(e7:Entity) " +
                        " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(30, totalGraphSize);
    }



}
