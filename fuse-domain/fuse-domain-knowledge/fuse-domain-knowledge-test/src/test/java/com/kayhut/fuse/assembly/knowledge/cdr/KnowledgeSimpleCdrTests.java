package com.kayhut.fuse.assembly.knowledge.cdr;

import com.kayhut.fuse.assembly.knowledge.Setup;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultBase;
import org.junit.*;

import java.io.IOException;
import java.util.List;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;

@Ignore("Run on nightly builds only")
public class KnowledgeSimpleCdrTests {
    static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(false);
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        long start = System.currentTimeMillis();
        long amount = DataLoader.load( ctx, "./data/cdr-small.csv");
        System.out.println(String.format("Loaded %d rows in %s ",amount,(System.currentTimeMillis()-start)/1000));
    }

//    @AfterClass
    public static void after() {
        ctx.removeCreated();
        ctx.clearCreated();
    }

    @Test
    public void testFetchPhoneRelationWithMultiVertices() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (value:Evalue {stringValue:'6671870408'})<-[rel:hasEvalue]-(phone:Entity)-[:hasRelation]->(rel:Relation)-[:hasRvalue]->(rValue:Rvalue) " +
                        " Where (rValue.fieldId = 'duration' AND rValue.stringValue = 58) Return *";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }

    @Test
    public void testFetchPhoneRelationWithMultiAndProperties() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity)-[:hasRelation]->(rel:Relation)-[:hasRvalue]->(rValue:Rvalue) " +
                        " Where (rValue.fieldId = 'duration' AND rValue.stringValue = 58) Return *";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }

    @Test
    public void testFetchPhoneRelationWithMultiOrProperties() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity)-[:hasRelation]->(rel:Relation)-[:hasRvalue]->(rValue:Rvalue) " +
                        " Where (rValue.fieldId = 'duration' OR rValue.stringValue = 58) Return *";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }

    @Test
    public void testFetchPhoneWithMultiPropertiesFilter() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity)-[rel:hasEvalue]->(value:Evalue) Where (value.stringValue = '6671870408' Or value.fieldId = 'location') Return *";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+(assignments.get(0).getEntities().size()-assignments.get(0).getRelationships().size()));

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }

    @Test
    public void testFetchPhoneRelToPhoneEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity )-[rel:hasEvalue]->(value:Evalue {stringValue:'6671870408'}) Return *";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+(assignments.get(0).getEntities().size()-assignments.get(0).getRelationships().size()));

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }

    @Test
    public void testFetchPhoneWithRelations() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity)-[rel:relatedEntity]->(any:Entity)  Return *";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Relationships fetched: "+assignments.get(0).getRelationships().size());
    }

    @Test
    public void testFetchPhoneWithRelationsAndCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity)-[rel:relatedEntity]->(any:Entity) Where (any.category = 'location') Return *";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Relationships fetched: "+assignments.get(0).getRelationships().size());
    }
}
