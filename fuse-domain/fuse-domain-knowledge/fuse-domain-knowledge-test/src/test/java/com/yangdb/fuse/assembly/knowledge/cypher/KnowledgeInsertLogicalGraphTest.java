package com.yangdb.fuse.assembly.knowledge.cypher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yangdb.fuse.model.logical.LogicalEdge;
import com.yangdb.fuse.model.logical.LogicalNode;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.countGraphElements;
import static com.yangdb.fuse.client.FuseClientSupport.nextPage;

/**
 * http://web.madstudio.northwestern.edu/re-visualizing-the-novel/
 * http://studentwork.prattsi.org/infovis/visualization/les-miserables-character-network-visualization/
 */
public class KnowledgeInsertLogicalGraphTest {

    //number of elements on les miserables graph


    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(true);
//        loadData();
    }

    private static void loadData() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("./data/logical/les_miserables.json");
        ResultResourceInfo info = fuseClient.loadData(KNOWLEDGE, resource);
        Assert.assertNotNull(info);
    }

    @Test
    public void testFetchEntityWithValuesGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) Return *";


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
        Assert.assertEquals(30, totalGraphSize);
    }


    @Test
    public void testFetchEntityWithRelationGraph() throws IOException, InterruptedException {
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
        Assert.assertEquals(37, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithValueGraphForSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue {stringValue: 'Myriel'}) Return *";


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
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(3, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithRelationGraphForSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[rHasEv:hasEvalue]->(ev:Evalue {stringValue: 'Myriel'}),  " +
                              "(e:Entity)-[rHasRel:hasRelation]->(rel:Relation) "+
                              " Return *";


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
        Assert.assertEquals(7, totalGraphSize);
    }


    @Test
    public void testFetchEntityWithRelationTwoStepGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                " (e2:Entity)-[r2:relatedEntity]->(e3:Entity) " +
                " Return *";


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
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(28, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithRelationThreeStepGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                " (e3:Entity)-[r3:relatedEntity]->(e4:Entity) " +
                " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(380, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithRelationThreeStepLogicalResultGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                " (e3:Entity)-[r3:relatedEntity]->(e4:Entity) " +
                " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphHierarchyCursorRequest(Arrays.asList("e1","e2","e3","e4"),new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(28, totalGraphSize);
    }
    @Test
    public void testFetchEntityWithRelationThreeStepLogicalResultGraphForSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                " (e1:Entity)-[rv:hasEvalue]->(ev:Evalue {stringValue: 'Myriel'}), " +
                " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                " (e3:Entity)-[r3:relatedEntity]->(e4:Entity) " +
                " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new CreateGraphHierarchyCursorRequest(Arrays.asList("e1","e2","e3","e4"),new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(19, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithLogicalGraphCursor() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                " (e1:Entity)-[rv:hasEvalue]->(ev:Evalue {stringValue: 'Myriel'}) " +
                " Return *";

        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new LogicalGraphCursorRequest(Arrays.asList("e1","e2","e3","e4"),new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        TypeReference<AssignmentsQueryResult<LogicalNode, LogicalEdge>> typeReference = new TypeReference<AssignmentsQueryResult<LogicalNode, LogicalEdge>>() {};
        AssignmentsQueryResult<LogicalNode, LogicalEdge> pageData = (AssignmentsQueryResult<LogicalNode, LogicalEdge>) nextPage(fuseClient, cursorResourceInfo, typeReference, 100);
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(3, pageData.getAssignments().get(0).getEntities().size());

        Optional<LogicalNode> node = pageData.getAssignments().get(0).getEntities().stream().filter(p -> p.getProperties().getProperties().containsKey("name")).findAny();
        Assert.assertTrue(node.isPresent());
        Assert.assertEquals(node.get().getProperties().getProperties().get("name"),"Myriel");
    }


}
