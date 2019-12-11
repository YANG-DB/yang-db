package com.yangdb.fuse.assembly.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.Setup;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.test.BaseITMarker;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.Setup.fuseClient;
import static com.yangdb.fuse.client.FuseClientSupport.query;

public class DragonsNestedNoConstraintsQueryIT implements BaseITMarker {
    public static final String DRAGONS = "Dragons";
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup();
        fuseClient = new BaseFuseClient("http://localhost:8888/fuse");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void after() {
//        Setup.cleanup();
    }

    @Test
    public void testLoadLogicalGraph() throws IOException, URISyntaxException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);

//        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraphWithNested.json");
        ResultResourceInfo<String> info = fuseClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
//        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Map map = (Map) new ObjectMapper().readValue(info.getResult(), Map.class).get("data");
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(2,((List)map.get("responses")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(0)).get("successes"));
        Assert.assertEquals(64,((List)((Map)((List)map.get("responses")).get(0)).get("successes")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(1)).get("successes"));
        Assert.assertEquals(64,((List)((Map)((List)map.get("responses")).get(1)).get("successes")).size());
    }

    private void initIndices() throws IOException {
        Map map = new ObjectMapper().readValue(fuseClient.initIndices(DRAGONS), Map.class);
        Assert.assertEquals(map.get("data").toString().trim(), "indices created:20");

        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
    }

    @Test
    public void testPersonWithName() throws IOException, InterruptedException, URISyntaxException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);

//        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraphWithNested.json");
        ResultResourceInfo<String> info = fuseClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
//        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 2, 0),
                        new Rel(2, "HasProfession", Rel.Direction.R, "k",3),
                        new ETyped(3, "pr", "Profession", 0, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

    }
    @Test
    public void testPersonHasProfession() throws IOException, InterruptedException, URISyntaxException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);

//        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraphWithNested.json");
        ResultResourceInfo<String> info = fuseClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
//        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 2, 0),
                        new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "Napoleon"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(1,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(0,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

    }

    @Test
    public void testPersonWithNestedProfessionKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = fuseClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 2, 0),
                        new Rel(2, "Know", Rel.Direction.R, "k",3),
                        new ETyped(3, "p2", "Person", 0, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

    }


}
