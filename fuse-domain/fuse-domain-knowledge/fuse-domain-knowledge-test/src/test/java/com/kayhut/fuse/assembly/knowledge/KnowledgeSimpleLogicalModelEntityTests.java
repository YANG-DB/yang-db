package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.*;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.transport.cursor.CreateLogicalGraphHierarchyCursorRequest;
import javaslang.collection.Stream;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

//@Ignore
public class KnowledgeSimpleLogicalModelEntityTests {
    static KnowledgeWriterContext ctx;

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
    public void testInsertOneSimpleEntityWithReferenceBuilder() throws IOException, InterruptedException {
        ValueBuilder v1 = _v(ctx.nextValueId()).field("title").value("Shirley Windzor");
        ValueBuilder v2 = _v(ctx.nextValueId()).field("nicknames").value("student");

        final EntityBuilder global = _e(ctx.nextLogicalId()).cat("person").ctx("global");
        global.value(v1,v2);

        final EntityBuilder e1 = _e(global.logicalId).cat("student").ctx("context1");
        e1.global(global);

        ValueBuilder v3 = _v(ctx.nextValueId()).field("job").value("blabla").ctx("context1").bdt("adf");
        ValueBuilder v4 = _v(ctx.nextValueId()).field("job").value("aaa").ctx("context1").bdt("qwer");
        ValueBuilder v5 = _v(ctx.nextValueId()).field("some field").value("jjjh").ctx("context1").bdt("qwer");
        e1.value(v3);
        e1.value(v4);
        e1.value(v5);

        // Create ref
        RefBuilder ref = _ref(ctx.nextRefId())
                .sys("sys")
                .title("some interesting monti")
                .url("http://someHosting/monti");
        //after ref is rendered add as a sub resource to the entity
        e1.reference(ref);



        //verify data inserted correctly
        Assert.assertEquals(2, commit(ctx, INDEX, global,e1));
        Assert.assertEquals(2, commit(ctx, INDEX, v1,v2));
        Assert.assertEquals(1, commit(ctx, REF_INDEX, ref));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = start()
                .withEntity(e1.getETag())
                .withValue("evalue")
                .withRef(ref.getETag())
                .withGlobalEntityValues(global.getETag())
                .build();

        // Read Entity (with V1 query)
//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query,
                new CreateLogicalGraphHierarchyCursorRequest(Arrays.asList(EntityBuilder.type)));

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
    public void testInsertOneSimpleEntityWithFileAndReferenceBuilder() throws IOException, InterruptedException {
        KnowledgeWriterContext ctx = KnowledgeWriterContext.init(client, manager.getSchema());
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
        Assert.assertEquals("hasEntityFile", assignments.get(0).getRelationships().get(1).getrType());

        Assert.assertEquals(3, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("File", assignments.get(0).getEntities().get(1).geteType());
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
        KnowledgeWriterContext ctx = KnowledgeWriterContext.init(client, manager.getSchema());
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
        Assert.assertEquals("hasEntityFile", assignments.get(0).getRelationships().get(0).getrType());

        Assert.assertEquals(2, assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity", assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("File", assignments.get(0).getEntities().get(1).geteType());

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
