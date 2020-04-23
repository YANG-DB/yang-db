package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RefBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.cursor.CreateForwardOnlyPathTraversalCursorRequest;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.junit.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.LogicalTypes.RELATED_ENTITY;
import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KnowledgeQueryBuilder.start;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder._ref;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.client.FuseClientSupport.query;

public class KnowledgeFindPathEntityWithRelationTests {
    static KnowledgeWriterContext ctx;
    static SimpleDateFormat sdf;
    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
    }

    @AfterClass
    public static void teardown() throws Exception {
//        Setup.cleanup(true,true);
    }

    @After
    public void after() {
        if(ctx!=null) ctx.removeCreated();
    }


    @Test
    public void testFindByConcretePathBuilderOneHop() throws IOException, InterruptedException, ParseException {
        String creationTime = "2018-07-17 13:19:20.667";
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("cat").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel_e1_e2 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).ctx("context1").cat("rel_e1_e2").creationTime(sdf.parse(creationTime));
        e1.rel(rel_e1_e2,"out");
        e2.rel(rel_e1_e2,"in");



        Assert.assertEquals(4, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel_e1_e2));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start()
                .withConcrete(e1.getETag(),e1.id() )
                .pathToEType(RELATED_ENTITY,EntityBuilder.type,0,3)
                .withRel(RELATED_ENTITY)
                .withConcrete(e2.getETag(),e2.id() )
                .build();

        AssignmentsQueryResult<Entity,Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) query(fuseClient, fuseResourceInfo, query,
                new CreateForwardOnlyPathTraversalCursorRequest());

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
    }

    @Test
    public void testFindByConcretePathBuilderTwoHop() throws IOException, InterruptedException, ParseException {
        String creationTime = "2018-07-17 13:19:20.667";
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("cat").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e3 = _e(ctx.nextLogicalId()).cat("dog").ctx("context1");
        final RelationBuilder rel_e1_e2 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).ctx("context1").cat("rel_e1_e2").creationTime(sdf.parse(creationTime));
        e1.rel(rel_e1_e2,"out");
        e2.rel(rel_e1_e2,"in");

        final RelationBuilder rel_e2_e3 = _rel(ctx.nextRelId()).sideA(e2).sideB(e3).ctx("context1").cat("rel_e2_e3").creationTime(sdf.parse(creationTime));
        e2.rel(rel_e2_e3,"out");
        e3.rel(rel_e2_e3,"in");


        Assert.assertEquals(7, commit(ctx, INDEX, e1,e2,e3));
        Assert.assertEquals(2, commit(ctx, REL_INDEX, rel_e1_e2,rel_e2_e3));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start()
                .withConcrete(e1.getETag(),e1.id() )
                .pathToEType(RELATED_ENTITY,EntityBuilder.type,0,3)
                .withRel(RELATED_ENTITY)
                .withConcrete(e3.getETag(),e3.id() )
                .build();

        AssignmentsQueryResult<Entity,Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) query(fuseClient, fuseResourceInfo, query,
                new CreateForwardOnlyPathTraversalCursorRequest());


        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());

        Assert.assertEquals(3, pageData.getAssignments().get(0).getEntities().size());
        Assert.assertEquals(2, pageData.getAssignments().get(0).getRelationships().size());

    }

    @Test
    public void testFindByConcretePathBuilderThreeHop() throws IOException, InterruptedException, ParseException {
        String creationTime = "2018-07-17 13:19:20.667";
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("cat").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e3 = _e(ctx.nextLogicalId()).cat("dog").ctx("context1");
        final EntityBuilder e4 = _e(ctx.nextLogicalId()).cat("fish").ctx("context1");
        final RelationBuilder rel_e1_e2 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).ctx("context1").cat("rel_e1_e2").creationTime(sdf.parse(creationTime));
        e1.rel(rel_e1_e2,"out");
        e2.rel(rel_e1_e2,"in");

        final RelationBuilder rel_e2_e3 = _rel(ctx.nextRelId()).sideA(e2).sideB(e3).ctx("context1").cat("rel_e2_e3").creationTime(sdf.parse(creationTime));
        e2.rel(rel_e2_e3,"out");
        e3.rel(rel_e2_e3,"in");

        final RelationBuilder rel_e3_e4 = _rel(ctx.nextRelId()).sideA(e3).sideB(e4).ctx("context1").cat("rel_e3_e4").creationTime(sdf.parse(creationTime));
        e3.rel(rel_e3_e4,"out");
        e4.rel(rel_e3_e4,"in");


        Assert.assertEquals(10, commit(ctx, INDEX, e1,e2,e3,e4));
        Assert.assertEquals(3, commit(ctx, REL_INDEX, rel_e1_e2,rel_e2_e3,rel_e3_e4));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start()
                .withConcrete(e1.getETag(),e1.id() )
                .pathToEType(RELATED_ENTITY,EntityBuilder.type,0,3)
                .withRel(RELATED_ENTITY)
                .withConcrete(e4.getETag(),e4.id() )
                .build();

        AssignmentsQueryResult<Entity,Relationship> pageData = (AssignmentsQueryResult<Entity, Relationship>) query(fuseClient, fuseResourceInfo, query,
                new CreateForwardOnlyPathTraversalCursorRequest());


        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());

        Assert.assertEquals(4, pageData.getAssignments().get(0).getEntities().size());
        Assert.assertEquals(3, pageData.getAssignments().get(0).getRelationships().size());


    }

}
