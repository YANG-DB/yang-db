package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.RelPattern;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EndPattern;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.CreatePageRequest;
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
import static com.yangdb.fuse.client.FuseClientSupport.*;
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
    @Ignore("Fails due to endPattern Etype(eType=Relation) cant be without continue of additionl Etype(eType=Entity) when in mid path ")
    public void testFindPathRelationCategory() throws IOException, InterruptedException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, "Start", "Entity","e00000006","e00000006", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3,4)),
                        new RelPattern(4, "relatedEntity",new com.yangdb.fuse.model.Range(1,3), R, null, 5, 0),
                        new EndPattern<>(new ETyped(5, Tagged.tagSeq("Middle"), "Relation", 6, 0)),
                        new EConcrete(6, "End-2", "Entity","e00000007","e00000007", 0, 0)
                )).build();
        AssignmentsQueryResult<Entity, Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) query(fuseClient, fuseResourceInfo, query,new CreateForwardOnlyPathTraversalCursorRequest());

        // Check if expected results and actual results are equal
        Assert.assertEquals(2, pageData.getSize());
        Assert.assertEquals(2, new HashSet<>(pageData.getAssignments()).size());

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
    @Test
    public void testGetSome3HopeRelationCategoryWithPages() throws IOException, InterruptedException {
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

        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        if(queryResourceInfo.getError()!=null) {
            Assert.assertTrue(queryResourceInfo.getError().getErrorDescription(),false);
        }

        // Press on Cursor
        CreateForwardOnlyPathTraversalCursorRequest cursorRequest = new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(2));
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, cursorRequest.getCreatePageRequest() != null ? cursorRequest.getCreatePageRequest().getPageSize() : 1000);
        // return the relevant data
        QueryResultBase data = fuseClient.getPageData(pageResourceInfo.getDataUrl());

        AssignmentsQueryResult<Entity, Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) data;

        // Check if expected results and actual results are equal
        Assert.assertEquals(2, pageData.getSize());

        QueryResultBase nextPage = nextPage(fuseClient, cursorResourceInfo, 2);

        // Check if expected results and actual results are equal
        Assert.assertEquals(3, nextPage.getSize());
    }


    @Test
    public void testGetSome5HopeRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, "Start", "Entity","e00000054","e00000054", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList( 4,9,18,30,46), 0),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R-1", "Relation", 6, 0),//relation
                        new Rel(6, "hasRelation", L, null, 7),
                        new EConcrete(7, "End-1", "Entity","e00000003","e00000003", 0, -1),
                        new Rel(9, "hasRelation", R, null, 10, 0),
                        new ETyped(10, "R-2", "Relation", 11, 0),//relation
                        new Rel(11, "hasRelation", L, null, 12),
                        new ETyped(12, "Middle-2", "Entity", 13, 0),
                        new Rel(13, "hasRelation", R, null, 14, 0),
                        new ETyped(14, "R-3", "Relation", 15, 0),//relation
                        new Rel(15, "hasRelation", L, null, 16),
                        new EConcrete(16, "End-2", "Entity","e00000003","e00000003", 0, -1),
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
                        new EConcrete(29, "End-3", "Entity","e00000003","e00000003", 0, -1),
                        new Rel(30, "hasRelation", R, null, 31, 0),
                        new ETyped(31, "R-7", "Relation", 32, 0),//relation
                        new Rel(32, "hasRelation", L, null, 33),
                        new ETyped(33, "Middle-5", "Entity", 34, 0),
                        new Rel(34, "hasRelation", R, null, 35, 0),
                        new ETyped(35, "R-8", "Relation", 36, 0),//relation
                        new Rel(36, "hasRelation", L, null, 37),
                        new ETyped(37, "Middle-6", "Entity", 38, 0),
                        new Rel(38, "hasRelation", R, null, 39, 0),
                        new ETyped(39, "R-9", "Relation", 40, 0),//relation
                        new Rel(40, "hasRelation", L, null, 41),
                        new ETyped(41, "Middle-7", "Entity", 42, 0),
                        new Rel(42, "hasRelation", R, null, 43, 0),
                        new ETyped(43, "R-10", "Relation", 44, 0),//relation
                        new Rel(44, "hasRelation", L, null, 45),
                        new EConcrete(45, "End-4", "Entity","e00000003","e00000003", 0, -1),
                        new Rel(46, "hasRelation", R, null, 47, 0),
                        new ETyped(47, "R-11", "Relation", 48, 0),//relation
                        new Rel(48, "hasRelation", L, null, 49),
                        new ETyped(49, "Middle-8", "Entity", 50, 0),
                        new Rel(50, "hasRelation", R, null, 51, 0),
                        new ETyped(51, "R-12", "Relation", 52, 0),//relation
                        new Rel(52, "hasRelation", L, null, 53),
                        new ETyped(53, "Middle-9", "Entity", 54, 0),
                        new Rel(54, "hasRelation", R, null, 55, 0),
                        new ETyped(55, "R-13", "Relation", 56, 0),//relation
                        new Rel(56, "hasRelation", L, null, 57),
                        new ETyped(57, "Middle-10", "Entity", 58, 0),
                        new Rel(58, "hasRelation", R, null, 59, 0),
                        new ETyped(59, "R-14", "Relation", 60, 0),//relation
                        new Rel(60, "hasRelation", L, null, 61),
                        new ETyped(61, "Middle-11", "Entity", 62, 0),
                        new Rel(62, "hasRelation", R, null, 63, 0),
                        new ETyped(63, "R-15", "Relation", 64, 0),//relation
                        new Rel(64, "hasRelation", L, null, 65),
                        new EConcrete(65, "End-5", "Entity","e00000003","e00000003", 0, -1)
                )).build();

        AssignmentsQueryResult<Entity, Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) query(fuseClient, fuseResourceInfo, query,new CreateForwardOnlyPathTraversalCursorRequest());
        AssignmentsQueryResult<Entity, Relationship> res= AssignmentsQueryResult.distinct(pageData);
        // Check if expected results and actual results are equal
        Assert.assertEquals(4, pageData.getSize());

    }
}
