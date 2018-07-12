package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.*;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.results.Entity;
import javaslang.collection.Stream;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.kayhut.fuse.assembly.knowledge.domain.FileBuilder._f;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KnowledgeQueryBuilder.start;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder._ref;
import static com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder._v;

public class KnowledgeSimpleEntityTests {
    static KnowledgeWriterContext ctx ;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
    }

    @After
    public void after()  {
        ctx.removeCreated();
        ctx.clearCreated();
    }

    @Test
    public void testInsertOneSimpleEntityWithBuilder() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        Assert.assertEquals(1, commit(ctx, INDEX, e1));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance().withEntity(e1.toEntity()).build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false,true);
    }

    @Test
    public void testInsertOneSimpleEntityWithValue() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        ValueBuilder v = _v(ctx.nextValueId()).field("name").value("Shirley Windzor").bdt("identifier");
        e1.value(v);
        Assert.assertEquals(1, commit(ctx, INDEX, e1));
        Assert.assertEquals(1, commit(ctx, INDEX, v));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).withValue(v.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(1, assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEvalue", assignments.get(0).getRelationships().get(0).getrType());

        Assert.assertEquals(2, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("Evalue", assignments.get(0).getEntities().get(1).geteType());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntities(e1.subEntities())//logicalEntity
                        .withRelationships(e1.withRelations())//relationships
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,false);
    }

    @Test
    public void testInsertOneSimpleEntityWithValues() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        ValueBuilder v1 = _v(ctx.nextValueId()).field("name").value("Shirley Windzor").bdt("identifier");
        ValueBuilder v2 = _v(ctx.nextValueId()).field("profession").value("student").bdt("occupation");
        e1.value(v1);
        e1.value(v2);
        Assert.assertEquals(1, commit(ctx, INDEX, e1));
        Assert.assertEquals(2, commit(ctx, INDEX, v1,v2));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).withValue(v1.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(2, assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEvalue", assignments.get(0).getRelationships().get(0).getrType());
        Assert.assertEquals("hasEvalue", assignments.get(0).getRelationships().get(1).getrType());

        Assert.assertEquals(3, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("Evalue", assignments.get(0).getEntities().get(1).geteType());
        Assert.assertEquals("Evalue", assignments.get(0).getEntities().get(2).geteType());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntities(e1.subEntities())//logicalEntity
                        .withRelationships(e1.withRelations())//relationships
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,true);
    }

    @Test
    public void testInsertEntityWithGlobalParent() throws IOException, InterruptedException {
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
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(3, ((AssignmentsQueryResult) pageData).getAssignments().get(0).getEntities().size());
        Assert.assertEquals(3, ((AssignmentsQueryResult) pageData).getAssignments().get(0).getRelationships().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntities(e1.subEntities())//logicalEntity
                        .withEntity(global.toEntity())//global entity
                        .withRelationships(e1.withRelations())//relationships
                        .withRelationships(e1.withRelations())//relationships (double relationships for the 2 different etags variations...
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,true);

    }

    @Test
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
        final List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

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
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntities(e1.subEntities())
                        .withRelationships(e1.withRelations())
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult.getAssignments().get(0).getEntities(), ((AssignmentsQueryResult) pageData).getAssignments().get(0).getEntities(), true);
        QueryResultAssert.assertEquals(expectedResult.getAssignments().get(0).getRelationships(), ((AssignmentsQueryResult) pageData).getAssignments().get(0).getRelationships(), true,true);

    }

    @Test
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
        final List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

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
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");

        // Create file
        FileBuilder file = _f(ctx.nextFileId())
                .path("file://data/mnt/root/")
                .name("monti").cat("word").mime("text").desc("Monti Python movie")
                .display("Monti-Pyhton");
        //after file is rendered add as a sub resource to the entity
        e1.file(file);

        //verify data inserted correctly
        Assert.assertEquals(1, commit(ctx, INDEX, e1));
        Assert.assertEquals(1, commit(ctx, INDEX, file));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = start()
                .withEntity(e1.getETag())
                .withFile(file.getETag())
                .build();

        // Read Entity (with V1 query)
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(1, assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEfile", assignments.get(0).getRelationships().get(0).getrType());

        Assert.assertEquals(2, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(1).geteType());
        Assert.assertEquals("Efile", assignments.get(0).getEntities().get(0).geteType());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())
                        .withEntities(e1.subEntities())
                        .withRelationships(e1.withRelations())
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true);

    }

}
