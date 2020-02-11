package com.yangdb.fuse.assembly.knowledge.cypher;

import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.nextPage;

/**
 * http://web.madstudio.northwestern.edu/re-visualizing-the-novel/
 * http://studentwork.prattsi.org/infovis/visualization/les-miserables-character-network-visualization/
 */
public class KnowledgeUploadCsvTest {

    //number of elements on les miserables graph

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(true);
    }


    @Test
    public void testLoadFileGraph() throws IOException, InterruptedException, URISyntaxException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasRelation]->(rel:Relation) Return *";

        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

        AssignmentsQueryResult pageData = (AssignmentsQueryResult) nextPage(fuseClient, cursorResourceInfo, 10000);
        int formerEntities = ((Assignment) pageData.getAssignments().get(0)).getEntities().size();
        int formerRelations = ((Assignment) pageData.getAssignments().get(0)).getRelationships().size();

        //load data
        URL people = Thread.currentThread().getContextClassLoader().getResource("data/csv/People.csv");
        ResultResourceInfo info = fuseClient.uploadCsvFile(KNOWLEDGE, "Entity", "Person", people);
        Assert.assertNotNull(info);
        Map map = mapper.readValue(info.getResult().toString(), Map.class);
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(6, ((List) ((Map) map.get("data")).get("responses")).size());
        //Entity count
        int entitiesCount = ((List) ((Map) (((List) ((Map) map.get("data")).get("responses")).get(0))).get("successes")).size();
        Assert.assertEquals(321, entitiesCount);
        //Evalue count
        int eValueCount = ((List) ((Map) (((List) ((Map) map.get("data")).get("responses")).get(2))).get("successes")).size();
        Assert.assertEquals(2247, eValueCount);


        URL knows = Thread.currentThread().getContextClassLoader().getResource("data/csv/Knows.csv");
        info = fuseClient.uploadCsvFile(KNOWLEDGE, "Relation", "Know", knows);
        Assert.assertNotNull(info);
        map = mapper.readValue(info.getResult().toString(), Map.class);
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(6, ((List) ((Map) map.get("data")).get("responses")).size());
        //RValue count
        int relEValueCount = ((List) ((Map) (((List) ((Map) map.get("data")).get("responses")).get(1))).get("successes")).size();
        Assert.assertEquals(7336, relEValueCount);
        //Relation count
        int relEntitiesCount = ((List) ((Map) (((List) ((Map) map.get("data")).get("responses")).get(3))).get("successes")).size();
        Assert.assertEquals(3668, relEntitiesCount);


        // get Query URL
        queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

        pageData = (AssignmentsQueryResult) nextPage(fuseClient, cursorResourceInfo, 10000);

        Assert.assertEquals(formerEntities + relEntitiesCount + entitiesCount, ((Assignment) pageData.getAssignments().get(0)).getEntities().size() );
        Assert.assertEquals(formerRelations + relEValueCount, ((Assignment) pageData.getAssignments().get(0)).getRelationships().size() );
    }


}
