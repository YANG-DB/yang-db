package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.cursor.CreateForwardOnlyPathTraversalCursorRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;

import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.query;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode.getClient;


@Ignore("Run against a live E/S loaded with following data: /data/raw/data-0-e0.json &  /data/raw/data-0-rel0.json ")
public class KnowledgeSimpleFindPathFunctionalTests {

    public static FuseClient fuseClient = null;
    public static TransportClient client = null;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
        client = getClient("knowledge", 9300);
        fuseClient = new BaseFuseClient("http://localhost:8888/fuse");

    }


    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testGetSomePathRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("query/findPath.json");
        Query findPathQuery = new com.fasterxml.jackson.databind.ObjectMapper().readValue(resource, Query.class);
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, findPathQuery,new CreateForwardOnlyPathTraversalCursorRequest());
        // Check if expected results and actual results are equal
        Assert.assertEquals(10, pageData.getSize());

    }

    @Test
    public void testGetSomeRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, "Start", "Entity","e00000006","e00000006", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList(4,  9), 0),
                            new Rel(4, "hasRelation", R, null, 5, 0),
                            new ETyped(5, "R-1", "Relation", 6, 0),//relation
                            new Rel(6, "hasRelation", L, null, 7),
                            new EConcrete(7, "End-1", "Entity","e00000007","e00000007", 0, -1),
                        new Rel(9, "hasRelation", R, null, 10, 0),
                            new ETyped(10, "R-2", "Relation", 11, 0),//relation
                            new Rel(11, "hasRelation", L, null, 12),
                            new ETyped(12, "Middle-2", "Entity", 13, 0),
                            new Rel(13, "hasRelation", R, null, 14, 0),
                            new ETyped(14, "R-3", "Relation", 15, 0),//relation
                            new Rel(15, "hasRelation", L, null, 16),
                            new EConcrete(16, "End-2", "Entity","e00000007","e00000007", 0, 0)
                )).build();
        AssignmentsQueryResult<Entity, Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) query(fuseClient, fuseResourceInfo, query,new CreateForwardOnlyPathTraversalCursorRequest());

        // Check if expected results and actual results are equal
        Assert.assertEquals(2, pageData.getSize());
        Assert.assertEquals(2, new HashSet<>(pageData.getAssignments()).size());
    }

    @Test
    public void testGetSome3HopeRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, "Start", "Entity","e00000006","e00000006", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList(4,  9, 18), 0),
                          new Rel(4, "hasRelation", R, null, 5, 0),
                            new ETyped(5, "R-1", "Relation", 6, 0),//relation
                            new Rel(6, "hasRelation", L, null, 7),
                            new EConcrete(7, "End-1", "Entity","e00000007","e00000007", 0, -1),
                        new Rel(9, "hasRelation", R, null, 10, 0),
                            new ETyped(10, "R-2", "Relation", 11, 0),//relation
                            new Rel(11, "hasRelation", L, null, 12),
                            new ETyped(12, "Middle-2", "Entity", 13, 0),
                            new Rel(13, "hasRelation", R, null, 14, 0),
                            new ETyped(14, "R-3", "Relation", 15, 0),//relation
                            new Rel(15, "hasRelation", L, null, 16),
                            new EConcrete(16, "End-2", "Entity","e00000007","e00000007", 0, 0),
                        new Rel(18, "hasRelation", R, null, 19, 0),
                            new ETyped(19, "R-4", "Relation", 20, 0),//relation
                            new Rel(20, "hasRelation", L, null, 21),
                            new ETyped(21, "Middle-3", "Entity", 22, 0),
                            new Rel(22, "hasRelation", R, null, 23, 0),
                            new ETyped(23, "R-5", "Relation", 24, 0),//relation
                            new Rel(24, "hasRelation", L, null, 25),
                            new ETyped(25, "Middle-4", "Entity", 26, 0),
                            new Rel(26, "hasRelation", R, null, 27, 0),
                            new ETyped(27, "R-6", "Relation", 28, 0),//relation
                            new Rel(28, "hasRelation", L, null, 29),
                            new EConcrete(29, "End-3", "Entity","e00000007","e00000007", 0, 0)
                )).build();

        AssignmentsQueryResult<Entity, Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) query(fuseClient, fuseResourceInfo, query,new CreateForwardOnlyPathTraversalCursorRequest());

        // Check if expected results and actual results are equal
        Assert.assertEquals(4, pageData.getSize());

    }
}
