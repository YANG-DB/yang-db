package com.yangdb.fuse.assembly.knowledge.cdr;

import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.query.ParameterizedQuery;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.*;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.client.FuseClientSupport.query;
import static com.yangdb.fuse.model.query.Rel.Direction.R;

public class KnowledgeSimpleCdrWithV1InnerQueryTests {
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @BeforeClass
    public static void setup() throws Exception {
//        KnowledgeSimpleCDR_TestSuite.setup();

    }

    @Test
    public void testInnerQueryInSet() throws IOException, InterruptedException {
        Query q1 = Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "fieldId", Constraint.of(ConstraintOp.eq, "phone"))
                )).build();

        Query q0 = Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "stringValue", InnerQueryConstraint.of(ConstraintOp.inSet, q1, "A2", "stringValue"))
                )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo, new CreateQueryRequest("q0", "q0", q0, new CreateGraphCursorRequest(new CreatePageRequest(100))));
        Query query = fuseClient.getQuery(graphResourceInfo.getV1QueryUrl(), ParameterizedQuery.class);
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[Entity:1]--> Rel(hasEvalue:2)──Typ[Evalue:3]──?[4]:[stringValue<inSet,[6672164961, 6671870408, 6673323922, internet.itelcel.com, 6671988978, 6671752136, 6671870406, 6672064796]>]]",
                QueryDescriptor.print(query));

        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(16, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        // return the relevant data
    }

    @Test
    public void testInnerQueryEq() throws IOException, InterruptedException {
        Query q1 = Query.Builder.instance().withName("Query" + "_testInnerQueryEq").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "fieldId", Constraint.of(ConstraintOp.eq, "phone"))
                )).build();

        Query q0 = Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "stringValue", InnerQueryConstraint.of(ConstraintOp.eq, q1, "A2", "stringValue"))
                )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo, new CreateQueryRequest("q0", "q0", q0,
                new CreateGraphCursorRequest(new CreatePageRequest(100))));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(16, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        Assert.assertEquals(8, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("relationships")).size());

        ParameterizedQuery query = (ParameterizedQuery) fuseClient.getQuery(graphResourceInfo.getV1QueryUrl(), ParameterizedQuery.class);
        Assert.assertEquals(8, ((List) ((List<NamedParameter>) query.getParams()).get(0).getValue()).size());

    }

    @Test
    public void testInnerQueryInRelativeRange() throws IOException, InterruptedException, ParseException {
        Query q1 = Query.Builder.instance().withName("Query" + "_testInnerQueryEq").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "fieldId", Constraint.of(ConstraintOp.eq, "phone"))
                )).build();

        long start = format.parse("01/01/2000 10:35:22").getTime();
        long end = format.parse("01/01/2025 13:24:19").getTime();

        Query q0 = Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "creationTime", InnerQueryConstraint.of(ConstraintOp.inRange,
                                new long[]{start, end}, WhereByFacet.JoinType.FOR_EACH, q1, "A2", "creationTime"))
                )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo, new CreateQueryRequest("q0", "q0", q0, new CreateGraphCursorRequest(new CreatePageRequest(100))));
        ParameterizedQuery query = (ParameterizedQuery) fuseClient.getQuery(graphResourceInfo.getV1QueryUrl(), ParameterizedQuery.class);
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[Entity:1]--> Rel(hasEvalue:2)──Typ[Evalue:3]──?[4]:[creationTime<inRange,[01-Jan-2000, 01-Jan-2025]>]]",
                QueryDescriptor.print(query));

        //
        //        Assert.assertEquals(8, ((List) ((List<NamedParameter>) query.getParams()).get(0).getValue()).size());
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(42, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        Assert.assertEquals(21, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("relationships")).size());
        // return the relevant data
    }

}
