package com.yangdb.cyber.ontology.schema;

import com.github.sisyphsu.dateparser.DateParser;
import com.yangdb.cyber.ontology.CyberTestSuiteIndexProviderSuite;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.test.BaseITMarker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import static com.yangdb.cyber.ontology.CyberTestSuiteIndexProviderSuite.app;
import static com.yangdb.cyber.ontology.CyberTestSuiteIndexProviderSuite.fuseClient;
import static com.yangdb.fuse.client.FuseClientSupport.query;

public class CyberQueryIT implements BaseITMarker {
    public static final String CYBER = "Cyber";
    static private SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    static private DateParser parser = DateParser.newBuilder().build();

    @BeforeClass
    public static void setup() throws Exception {
        CyberTestSuiteIndexProviderSuite.setup(false, CYBER);//todo remove remark when running IT tests
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void after() {
//        Setup.cleanup();
        if (app != null) {
            app.stop();
        }
    }



    @Test
    public void testQueryTraces() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);


        Query query = Query.Builder.instance().withName("query").withOnt(CYBER)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "trace", "traces", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5)),
                        new EProp(3, "trace_status", Constraint.of(ConstraintOp.eq,0)),
                        new EProp(4, "trace_type", Constraint.of(ConstraintOp.eq,"Sequence based")),
                        new EProp(5, "status_update_time", Constraint.of(ConstraintOp.ge,parser.parseDate("2018-10-01 11:42")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(17, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
    }

    @Test
    public void testQueryBehaviors() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);


        Query query = Query.Builder.instance().withName("query").withOnt(CYBER)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "behavior", "behaviors", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5)),
                        new EProp(3, "by_type_id", Constraint.of(ConstraintOp.eq,1)),
                        new EProp(4, "by_name", Constraint.of(ConstraintOp.like,"*.exe")),
                        new EProp(5, "insert_time", Constraint.of(ConstraintOp.ge,parser.parseDate("2018-10-01 11:42")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(472, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());

    }

    @Test
    public void testQueryTraceToBehaviors() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);


        Query query = Query.Builder.instance().withName("query").withOnt(CYBER)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "trace", "traces", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5, 6)),
                            new EProp(3, "trace_status", Constraint.of(ConstraintOp.eq,0)),
                            new EProp(4, "trace_type", Constraint.of(ConstraintOp.eq,"Sequence based")),
                            new EProp(5, "status_update_time", Constraint.of(ConstraintOp.ge,parser.parseDate("2018-10-01 11:42"))),
                        new Rel(6, "tracestobehaviors", Rel.Direction.R, "hasBehavior", 7),
                            new ETyped(7, "behavior", "behaviors", 8, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query,new CreateGraphCursorRequest(new CreatePageRequest()));

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(6, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(4, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());
    }

    @Test
    public void testQueryTraceToAll() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);


        Query query = Query.Builder.instance().withName("query").withOnt(CYBER)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "trace", "traces", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5, 6)),
                            new EProp(3, "trace_status", Constraint.of(ConstraintOp.eq,0)),
                            new EProp(4, "trace_type", Constraint.of(ConstraintOp.eq,"Sequence based")),
                            new EProp(5, "status_update_time", Constraint.of(ConstraintOp.ge,parser.parseDate("2018-10-01 11:42"))),
                        new Rel(6, "*", Rel.Direction.R, "hasRelation", 7),
                            new EUntyped(7, "any", 8, 0)
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query,new CreateGraphCursorRequest(new CreatePageRequest()));

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(6, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(4, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());
    }
}
