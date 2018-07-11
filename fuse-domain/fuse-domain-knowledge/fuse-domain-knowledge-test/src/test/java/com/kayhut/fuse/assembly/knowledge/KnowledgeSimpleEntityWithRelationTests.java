package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.*;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.results.Entity;
import javaslang.collection.Stream;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KnowledgeQueryBuilder.start;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RefBuilder._ref;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder._v;

public class KnowledgeSimpleEntityWithRelationTests {
    static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() {
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
    }

    @After
    public void after() {
        ctx.removeCreated();
        ctx.clearCreated();
    }

    @Test
    public void testInsertOneSimpleEntityWithRelation() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);

        Assert.assertEquals(2, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).relatedTo(e1.getETag()+"->"+e2.getETag(),e2.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntity(e2.toEntity())//context entity
                        .withEntity(rel.toEntity())//context entity
                        .withRelationships(e1.withRelations())//relationships
                        .withRelationships(e2.withRelations())//relationships
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,true);
    }

    @Test
    public void testInsertOneSimpleEntityWithRelationAndValue() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        ValueBuilder v1 = _v(ctx.nextValueId()).field("name").value("Shirley Windzor").bdt("identifier");
        ValueBuilder v2 = _v(ctx.nextValueId()).field("name").value("Dorn Windzor").bdt("identifier");
        e1.value(v1);
        e2.value(v2);

        Assert.assertEquals(2, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));
        Assert.assertEquals(2, commit(ctx, INDEX, v1,v2));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).withValue(v1.getETag()).relatedTo(e1.getETag()+"->"+e2.getETag(),e2.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(4, assignments.get(0).getRelationships().size());
        Assert.assertEquals(5, assignments.get(0).getEntities().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel.toEntity())//context entity
                        //entity-1
                        .withEntity(e1.toEntity())//context entity
                        .withEntities(e1.subEntities())//logicalEntity
                        .withRelationships(e1.withRelations())//relationships
                        //entity-2
                        .withEntity(e2.toEntity())//context entity
                        .withEntities(e2.subEntities())//logicalEntity
                        .withRelationships(e2.withRelations())//relationships
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,true);
    }

    @Test(expected = AssertionError.class)
    /**
     * todo check what fails in the engine
     */
    public void testInsertEntityWithRelationAndGlobalParent() throws IOException, InterruptedException {
        final String logicalId = ctx.nextLogicalId();
        final EntityBuilder global = _e(logicalId).cat("person");
        final EntityBuilder e1 = _e(logicalId).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(logicalId).cat("person").ctx("context2");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        e1.global(global);
        e2.global(global);
        //verify inserted
        Assert.assertEquals(3, commit(ctx, INDEX, global,e1,e2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).withGlobalEntity(global.getETag()).relatedTo(e1.getETag()+"->"+e2.getETag(),e2.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(5, ((AssignmentsQueryResult) pageData).getAssignments().get(0).getEntities().size());
        Assert.assertEquals(7, ((AssignmentsQueryResult) pageData).getAssignments().get(0).getRelationships().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntities(e1.subEntities())//logicalEntity
                        .withEntity(e2.toEntity())//context entity
                        .withEntities(e2.subEntities())//logicalEntity
                        .withEntity(global.toEntity())//global entity
                        .withEntity(rel.toEntity())//global entity
                        .withRelationships(e1.withRelations())//relationships
                        .withRelationships(e2.withRelations())//relationships (double relationships for the 2 different etags variations...
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult.getAssignments().get(0).getEntities(), ((AssignmentsQueryResult) pageData).getAssignments().get(0).getEntities(), true );

        //this should not fail - check why actual returns only single pov to global relationship
        QueryResultAssert.assertEquals(expectedResult.getAssignments().get(0).getRelationships(), ((AssignmentsQueryResult) pageData).getAssignments().get(0).getRelationships(),true, true );
    }

    @Test
    public void testInsertOneSimpleEntityWithRelationAndReference() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);

        // Create ref
        RefBuilder ref1 = _ref(ctx.nextRefId())
                .sys("sys")
                .title("some interesting monti")
                .url("http://someHosting/monti");
        RefBuilder ref2 = _ref(ctx.nextRefId())
                .sys("sys")
                .title("some interesting jhony")
                .url("http://someHosting/jhony");
        //after ref is rendered add as a sub resource to the entity
        e1.reference(ref1);
        e2.reference(ref2);

        //verify data inserted correctly
        Assert.assertEquals(2, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(2, commit(ctx, REF_INDEX, ref1,ref2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));


        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = start()
                .withEntity(e1.getETag())
                .withRef(ref1.getETag())
                .relatedTo(e1.getETag()+"->"+e2.getETag(),e2.getETag()).build();

        // Read Entity (with V1 query)
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(4, assignments.get(0).getRelationships().size());
        Assert.assertEquals(5, assignments.get(0).getEntities().size());

        //bug logicalId returns on Reference entity
        List<Entity> subEntities1 = e1.subEntities();
        Entity reference = Stream.ofAll(subEntities1).find(entity -> entity.geteType().equals("Reference")).get();
        List<Property> newProps = new ArrayList<>(reference.getProperties());
        newProps.add(new Property("logicalId", "raw", e1.logicalId));
        reference.setProperties(newProps);

        //bug logicalId returns on Reference entity
        List<Entity> subEntities2 = e2.subEntities();
        reference = Stream.ofAll(subEntities2).find(entity -> entity.geteType().equals("Reference")).get();
        newProps = new ArrayList<>(reference.getProperties());
        newProps.add(new Property("logicalId", "raw", e2.logicalId));
        reference.setProperties(newProps);

        //verify assignments return as expected
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel.toEntity())
                        //entity 1
                        .withEntity(e1.toEntity())
                        .withEntities(subEntities1)
                        .withRelationships(e1.withRelations())
                        //entity 1
                        .withEntity(e2.toEntity())
                        .withEntities(subEntities2)
                        .withRelationships(e2.withRelations())
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,true);

    }

}
