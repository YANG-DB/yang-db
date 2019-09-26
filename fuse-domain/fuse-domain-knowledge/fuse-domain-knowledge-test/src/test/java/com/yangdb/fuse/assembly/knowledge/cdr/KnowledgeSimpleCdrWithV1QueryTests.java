package com.yangdb.fuse.assembly.knowledge.cdr;

import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.QueryResultBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.model.query.Rel.Direction.R;

public class KnowledgeSimpleCdrWithV1QueryTests {

    @Test
    public void testSimpleQuery() throws IOException, InterruptedException {
        Query q1 = Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 3, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3),-1),
                        new Rel(3, "hasEvalue", R, null, 4, 0),
                        new ETyped(4, "A2", "Evalue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6),-1),
                        new EPropGroup(6,
                            new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq,"laborum.")),
                            new EProp(8, "stringValue", Constraint.of(ConstraintOp.eq,"commodo")))
                )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), q1);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 10);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        // return the relevant data
        QueryResultBase pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(pageData);
        Assert.assertTrue(pageData.getSize()>0);

    }
}
