package com.yangdb.fuse.assembly.knowledge.cypher;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.domain.*;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.results.Entity;
import javaslang.collection.Stream;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.FileBuilder._f;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KnowledgeQueryBuilder.start;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder._ref;
import static com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder._v;

public class KnowledgeSimpleEntityTests {
    static KnowledgeWriterContext ctx;
    static EntityBuilder e0;
    static EntityBuilder e1;
    static EntityBuilder e2;
    static ValueBuilder v1;
    static ValueBuilder v2;
    static ValueBuilder v3;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(true);
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        e0 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        e1 =_e(ctx.nextLogicalId()).cat("person").ctx("context1");
        v1 = _v(ctx.nextValueId()).field("name").value("Shirley Windzor").bdt("identifier");

        e1.value(v1);

        e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        v2 = _v(ctx.nextValueId()).field("name").value("Shirley Windzor").bdt("identifier");
        v3 = _v(ctx.nextValueId()).field("profession").value("student").bdt("occupation");

        e2.value(v2);
        e2.value(v3);

        Assert.assertEquals(3, commit(ctx, INDEX, e0,e1,e2));
        Assert.assertEquals(3, commit(ctx, INDEX, v1,v2,v3));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
        ctx.clearCreated();
    }

    @Test
    public void testInsertOneSimpleEntityWithBuilder() throws IOException, InterruptedException {

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e0.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e0.toEntity())
                        .withEntity(e1.toEntity())
                        .withEntity(e2.toEntity())
                        .build())
                .build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false, true);
    }

    @Test
    public void testInsertOneSimpleEntityWithValue() throws IOException, InterruptedException {

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).withValue(v1.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment<Entity,Relationship>> assignments = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(3, assignments.get(0).getRelationships().size());
        Assert.assertEquals(3, assignments.get(0).getRelationships().stream().filter(e->e.getrType().equals("hasEvalue")).count());

        Assert.assertEquals(5, assignments.get(0).getEntities().size());
        Assert.assertEquals(3, assignments.get(0).getEntities().stream().filter(e->e.geteType().equals("Evalue")).count());
        Assert.assertEquals(2, assignments.get(0).getEntities().stream().filter(e->e.geteType().equals("Entity")).count());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntity(e2.toEntity())//context entity
                        .withEntities(e1.subEntities())//logicalEntity
                        .withEntities(e2.subEntities())//logicalEntity
                        .withRelationships(e1.withRelations())//relationships
                        .withRelationships(e2.withRelations())//relationships
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }


    @Test
    public void testInsertOneSimpleEntityWithValuesWithCypher() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
//        Query query = start().withEntity(e1.getETag()).withValue(v1.getETag()).build();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) Return e,r,ev";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);
        List<Assignment<Entity,Relationship>> assignments = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments();

        // Check Entity Response
        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(3, assignments.get(0).getRelationships().size());
        Assert.assertEquals(3, assignments.get(0).getRelationships().stream().filter(e->e.getrType().equals("hasEvalue")).count());

        Assert.assertEquals(5, assignments.get(0).getEntities().size());
        Assert.assertEquals(3, assignments.get(0).getEntities().stream().filter(e->e.geteType().equals("Evalue")).count());
        Assert.assertEquals(2, assignments.get(0).getEntities().stream().filter(e->e.geteType().equals("Entity")).count());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntity(e2.toEntity())//context entity
                        .withEntities(e1.subEntities())//logicalEntity
                        .withEntities(e2.subEntities())//logicalEntity
                        .withRelationships(e1.withRelations())//relationships
                        .withRelationships(e2.withRelations())//relationships
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true, true);
    }

    @Test
    public void testInsertEntityWithGlobalParentNoResultsSinceNoValuesFound() throws IOException, InterruptedException {
        final String logicalId = ctx.nextLogicalId();
        final EntityBuilder global = _e(logicalId).cat("person");
        final EntityBuilder e1 = _e(logicalId).cat("student").ctx("context1");
        e1.global(global);
        //verify inserted
        Assert.assertEquals(1, commit(ctx, INDEX, global));
        Assert.assertEquals(1, commit(ctx, INDEX, e1));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).withGlobalEntity(global.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().size());
        Assert.assertEquals(0, ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities().size());
        Assert.assertEquals(0, ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getRelationships().size());

    }

    @Test
    @Ignore("TODO: fix reference logical id bug")
    public void testInsertOneSimpleEntityWithReferenceBuilder() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");

        // Create ref
        RefBuilder ref = _ref(ctx.nextRefId())
                .sys("sys")
                .title("some interesting monti")
                .url("http://someHosting/monti");
        //after ref is rendered add as a sub resource to the entity
        e1.reference(ref);

        //verify data inserted correctly
        Assert.assertEquals(1, commit(ctx, INDEX, e1));
        Assert.assertEquals(1, commit(ctx, REF_INDEX, ref));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = start()
                .withEntity(e1.getETag())
                .withRef(ref.getETag())
                .build();

        // Read Entity (with V1 query)
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment<Entity,Relationship>> assignments = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(1, assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEntityReference", assignments.get(0).getRelationships().get(0).getrType());

        Assert.assertEquals(2, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("Reference", assignments.get(0).getEntities().get(1).geteType());

        //bug logicalId returns on Reference entity
/*
        List<Entity> subEntities = e1.subEntities();
        Entity reference = Stream.ofAll(subEntities).find(entity -> entity.geteType().equals("Reference")).get();
        List<Property> newProps = new ArrayList<>(reference.getProperties());
        newProps.add(new Property("logicalId", "raw", e1.logicalId));
        reference.setProperties(newProps);
*/

        //verify assignments return as expected
        AssignmentsQueryResult<Entity,Relationship> expectedResult = AssignmentsQueryResult.Builder.<Entity,Relationship>instance()
                .withAssignment(Assignment.Builder.<Entity,Relationship>instance()
                        .withEntity(e1.toEntity())
                        .withEntities(e1.subEntities())
                        .withRelationships(e1.withRelations())
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult.getAssignments().get(0).getEntities(), ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getEntities(), true);
        QueryResultAssert.assertEquals(expectedResult.getAssignments().get(0).getRelationships(), ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().get(0).getRelationships(), true, true);

    }

    @Test
    @Ignore
    public void testInsertOneSimpleEntityWithFileAndReferenceBuilder() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");

        // Create file
        FileBuilder file = _f(ctx.nextFileId())
                .path("file://data/mnt/root/")
                .name("monti").cat("word").mime("text").desc("Monti Python movie")
                .display("Monti-Pyhton");
        //after file is rendered add as a sub resource to the entity
        e1.file(file);

        // Create ref
        RefBuilder ref = _ref(ctx.nextRefId())
                .sys("sys")
                .title("some interesting monti")
                .url("http://someHosting/monti");
        //after ref is rendered add as a sub resource to the entity
        e1.reference(ref);

        //verify data inserted correctly
        Assert.assertEquals(1, commit(ctx, INDEX, file));
        Assert.assertEquals(1, commit(ctx, INDEX, e1));
        Assert.assertEquals(1, commit(ctx, REF_INDEX, ref));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = start()
                .withEntity(e1.getETag())
                .withRef(ref.getETag())
                .withFile(file.getETag())
                .build();

        // Read Entity (with V1 query)
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment<Entity,Relationship>> assignments = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(2, assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEntityReference", assignments.get(0).getRelationships().get(0).getrType());
        Assert.assertEquals("hasEfile", assignments.get(0).getRelationships().get(1).getrType());

        Assert.assertEquals(3, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(1).geteType());
        Assert.assertEquals("Efile", assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("Reference", assignments.get(0).getEntities().get(2).geteType());

        //bug logicalId returns on Reference entity
        List<Entity> subEntities = e1.subEntities();
        Entity reference = Stream.ofAll(subEntities).find(entity -> entity.geteType().equals("Reference")).get();
        List<Property> newProps = new ArrayList<>(reference.getProperties());
        newProps.add(new Property("logicalId", "raw", e1.logicalId));
        reference.setProperties(newProps);

        //verify assignments return as expected
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntities(subEntities)
                        .withRelationships(e1.withRelations())
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true);

    }

    @Test
    public void testInsertEntityWithFileWithBuilder() throws IOException, InterruptedException {
        // Create file
        FileBuilder file = _f(ctx.nextFileId())
                .path("file://data/mnt/root/")
                .name("monti").cat("word").mime("text").desc("Monti Python movie")
                .display("Monti-Pyhton");
        //after file is rendered add as a sub resource to the entity
        e0.file(file);

        //verify data inserted correctly
        Assert.assertEquals(1, commit(ctx, INDEX, e0));
        Assert.assertEquals(1, commit(ctx, INDEX, file));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = start()
                .withEntity(e0.getETag())
                .withFile(file.getETag())
                .build();

        // Read Entity (with V1 query)
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment<Entity,Relationship>> assignments = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(1, assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEfile", assignments.get(0).getRelationships().get(0).getrType());

        Assert.assertEquals(2, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(1).geteType());
        Assert.assertEquals("Efile", assignments.get(0).getEntities().get(0).geteType());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e0.toEntity())
                        .withEntity(file.toEntity())
                        .withRelationships(e0.withRelations())
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true);

    }

}
