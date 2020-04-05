package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.RelPattern;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EndPattern;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.cursor.CreateForwardOnlyPathTraversalCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.yangdb.fuse.model.transport.cursor.FindPathTraversalCursorRequest;
import org.junit.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.model.query.Rel.Direction.R;

public class KnowledgeSimpleEntityWithAnyDegreeOfRelationTests {
    static KnowledgeWriterContext ctx;
    static SimpleDateFormat sdf;

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(false,true);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        //Setup.setup();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
    }

    @After
    public void after() {
        if(ctx!=null) ctx.removeCreated();
    }

    @Test
    public void testSimpleEntityWithRelationFindPathInUpTo2Distance() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e3 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e4 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel1 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        e1.rel(rel1,"out");
        e2.rel(rel1,"in");

        final RelationBuilder rel2 = _rel(ctx.nextRelId()).sideA(e2).sideB(e3);
        e2.rel(rel2,"out");
        e3.rel(rel2,"in");

        final RelationBuilder rel3 = _rel(ctx.nextRelId()).sideA(e3).sideB(e4);
        e3.rel(rel3,"out");
        e4.rel(rel3,"in");


        Assert.assertEquals(10, commit(ctx, INDEX, e1,e2,e3,e4));
        Assert.assertEquals(3, commit(ctx, REL_INDEX, rel1,rel2,rel3));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new RelPattern(2, "relatedEntity",new com.yangdb.fuse.model.Range(1,2), R, null, 3, 0),
                        new EndPattern<>(new ETyped(3, Tagged.tagSeq("B"), "Entity", 0, 0))
                )).build();
//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        QueryResultBase pageData =  query(fuseClient, fuseResourceInfo, query, new CreateForwardOnlyPathTraversalCursorRequest());

        // Check Entity Response
        Assert.assertEquals(16, pageData.getSize());

    }

    @Test
    public void testSimpleEntityWithRelationFindPathInUpTo3Distance() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("start");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e3 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e4 = _e(ctx.nextLogicalId()).cat("person").ctx("end");
        final RelationBuilder rel1 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        e1.rel(rel1,"out");
        e2.rel(rel1,"in");

        final RelationBuilder rel2 = _rel(ctx.nextRelId()).sideA(e2).sideB(e3);
        e2.rel(rel2,"out");
        e3.rel(rel2,"in");

        final RelationBuilder rel3 = _rel(ctx.nextRelId()).sideA(e3).sideB(e4);
        e3.rel(rel3,"out");
        e4.rel(rel3,"in");


        Assert.assertEquals(10, commit(ctx, INDEX, e1,e2,e3,e4));
        Assert.assertEquals(3, commit(ctx, REL_INDEX, rel1,rel2,rel3));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "Source", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3,4)),
                        new EProp(3,"context",Constraint.of(ConstraintOp.eq,"start")),
                        new RelPattern(4, "relatedEntity",new com.yangdb.fuse.model.Range(1,3), R, null, 5, 0),
                        new EndPattern<>(new ETyped(5, Tagged.tagSeq("Target"), "Entity", 6, 0)),
                        new EProp(6,"context",Constraint.of(ConstraintOp.eq,"end"))
                )).build();
//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        QueryResultBase pageData =  query(fuseClient, fuseResourceInfo, query, new FindPathTraversalCursorRequest(1));

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());

    }
    @Test
    public void testSimpleEntityWithRelationFindPathInUpTo2DistanceShouldFail() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("start");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e3 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e4 = _e(ctx.nextLogicalId()).cat("person").ctx("end");
        final RelationBuilder rel1 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        e1.rel(rel1,"out");
        e2.rel(rel1,"in");

        final RelationBuilder rel2 = _rel(ctx.nextRelId()).sideA(e2).sideB(e3);
        e2.rel(rel2,"out");
        e3.rel(rel2,"in");

        final RelationBuilder rel3 = _rel(ctx.nextRelId()).sideA(e3).sideB(e4);
        e3.rel(rel3,"out");
        e4.rel(rel3,"in");


        Assert.assertEquals(10, commit(ctx, INDEX, e1,e2,e3,e4));
        Assert.assertEquals(3, commit(ctx, REL_INDEX, rel1,rel2,rel3));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "Source", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3,4)),
                        new EProp(3,"context",Constraint.of(ConstraintOp.eq,"start")),
                        new RelPattern(4, "relatedEntity",new com.yangdb.fuse.model.Range(1,2), R, null, 5, 0),
                        new EndPattern<>(new ETyped(5, Tagged.tagSeq("Target"), "Entity", 6, 0)),
                        new EProp(6,"context",Constraint.of(ConstraintOp.eq,"end"))
                )).build();
//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        QueryResultBase pageData =  query(fuseClient, fuseResourceInfo, query, new FindPathTraversalCursorRequest(1));

        // Check Entity Response
        Assert.assertEquals(0, pageData.getSize());

    }

    @Test
    public void testSimpleEntityWithRelation() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e3 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e4 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel1 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        e1.rel(rel1,"out");
        e2.rel(rel1,"in");

        final RelationBuilder rel2 = _rel(ctx.nextRelId()).sideA(e2).sideB(e3);
        e2.rel(rel2,"out");
        e3.rel(rel2,"in");

        final RelationBuilder rel3 = _rel(ctx.nextRelId()).sideA(e3).sideB(e4);
        e3.rel(rel3,"out");
        e4.rel(rel3,"in");


        Assert.assertEquals(10, commit(ctx, INDEX, e1,e2,e3,e4));
        Assert.assertEquals(3, commit(ctx, REL_INDEX, rel1,rel2,rel3));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Rel(2, "relatedEntity", R, null, 3, 0),
                        new ETyped(3, Tagged.tagSeq("B"), "Entity", 0, 0)
                )).build();
        QueryResultBase pageData =  query(fuseClient, fuseResourceInfo, query, new CreatePathsCursorRequest());

        // Check Entity Response
        Assert.assertEquals(6, pageData.getSize());

    }
}
