package com.yangdb.fuse.assembly.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.Setup;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.schema.Relation;
import com.yangdb.test.BaseITMarker;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.Setup.fuseClient;
import static com.yangdb.fuse.client.FuseClientSupport.query;

public class DragonsSimpleConstraintsQueryIT implements BaseITMarker {
    public static final String DRAGONS = "Dragons";
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void after() {
//        Setup.cleanup();
    }

    @Test
    public void testPersonKnowsPersonProjection() throws IOException, InterruptedException, URISyntaxException {
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
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4)),
                        new EProp(3, "firstName", new IdentityProjection()),
                        new Rel(4, "Know", Rel.Direction.R, "k", 5),
                        new ETyped(5, "p2", "Person", 6, 0),
                        new EProp(6, "firstName", new IdentityProjection())
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(3, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());

        //validate projection only returned the single explicit firstName field
        Assignment<Entity, Relationship> assignment = (Assignment<Entity, Relationship>) ((AssignmentsQueryResult) pageData).getAssignments().get(0);
        assignment.getEntities().forEach(e-> {
            Assert.assertEquals(1,e.getProperties().size());
            Assert.assertEquals("firstName",e.getProperties().iterator().next().getpType());
        });

    }
    @Test
    public void testPersonKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
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
                        new Quant1(2, QuantType.all, Arrays.asList(3, 5)),
                        new Rel(3, "Know", Rel.Direction.R, "k", 4),
                        new ETyped(4, "p2", "Person", 0, 0),
                        new EProp(5, "firstName", Constraint.of(ConstraintOp.eq, "Napoleon"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(2, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());

    }

    private void initIndices() throws IOException {
        Map map = new ObjectMapper().readValue(fuseClient.initIndices(DRAGONS), Map.class);
        Assert.assertEquals(map.get("data").toString().trim(), "indices created:20");
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
    }

    @Test
    public void testPersonOwnsDragonAndKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
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
                        new ETyped(1, "p1", "Person", 100, 0),
                        new Quant1(100, QuantType.all, Arrays.asList(2, 4)),
                        new Rel(2, "Know", Rel.Direction.R, "know", 3),
                        new ETyped(3, "p2", "Person", 0, 0),
                        new Rel(4, "Own", Rel.Direction.R, "own", 5),
                        new ETyped(5, "d2", "Dragon", 6, 0),
                        new EProp(6, "power", Constraint.of(ConstraintOp.gt, 100))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(3, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(2, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());
        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Know")).count());

        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(2, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());

    }

    @Test
    public void testDragonOwnedByPerson() throws IOException, InterruptedException, URISyntaxException {
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
                        new ETyped(1, "d1", "Dragon", 100, 0),
                        new Quant1(100, QuantType.all, Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.L, "own", 3,20),
                        new RelProp(20,"startDate",Constraint.of(ConstraintOp.gt,"1793-04-13 22:00:00.000")),
                        new ETyped(3, "p1", "Person", 0, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(2, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());

        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());

    }

    @Test
    public void testPersonOwnsDragon() throws IOException, InterruptedException, URISyntaxException {
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
                        new ETyped(1, "p1", "Person", 100, 0),
                        new Quant1(100, QuantType.all, Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.R, "own", 3,20),
                        new RelProp(20,"startDate",Constraint.of(ConstraintOp.gt,"1793-04-13 22:00:00.000")),
                        new ETyped(3, "d1", "Dragon", 0, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(2, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());

        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());

    }


    @Test
    public void testPersonOwnsDragonOrKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
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
                        new ETyped(1, "p1", "Person", 100, 0),
                        new Quant1(100, QuantType.some, Arrays.asList(2, 4)),
                        new Rel(2, "Know", Rel.Direction.R, "know", 3),
                            new ETyped(3, "p2", "Person", 30, 0),
                            new EProp(30, "firstName", Constraint.of(ConstraintOp.eq, "Napoleon")),
                        new Rel(4, "Own", Rel.Direction.R, "own", 5),
                            new ETyped(5, "d2", "Dragon", 50, 0),
                            new EProp(50, "birthDate", Constraint.of(ConstraintOp.gt, "600-01-01 22:00:00.000"))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(5, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(2, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());
        Assert.assertEquals(1, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Know")).count());

        Assert.assertEquals(2, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(3, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());

    }
}
