package com.kayhut.fuse.assembly.knowledge.cdr;

import com.kayhut.fuse.assembly.knowledge.Setup;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

public class KnowledgeSimpleCdrWithV1InnerQueryTests {
    static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(false,true);
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        long start = System.currentTimeMillis();
        long amount = DataLoader.load( ctx, "./data/cdr/cdr-small.csv");
        System.out.println(String.format("Loaded %d rows in %s ",amount,(System.currentTimeMillis()-start)/1000));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
        ctx.clearCreated();
        fuseClient.shutdown();
    }

    @Test
    public void testInnerQuery() throws IOException, InterruptedException {
        Query q1 = Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "fieldId", Constraint.of(ConstraintOp.eq,"phone"))
                )).build();

        Query q0 = Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "stringValue", InnerQueryConstraint.of(ConstraintOp.inSet,q1,"A2.stringValue"))
                )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo, new CreateQueryRequest("q0", "q0", q0, new CreateGraphCursorRequest(new CreatePageRequest(100))));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(16, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        // return the relevant data
    }

}
