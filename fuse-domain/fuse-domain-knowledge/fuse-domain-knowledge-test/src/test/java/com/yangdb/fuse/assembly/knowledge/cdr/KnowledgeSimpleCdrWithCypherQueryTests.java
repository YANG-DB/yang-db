package com.yangdb.fuse.assembly.knowledge.cdr;

import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;

public class KnowledgeSimpleCdrWithCypherQueryTests {
    static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        long start = System.currentTimeMillis();
        long amount = DataLoader.load( ctx, "data/cdr/cdr-small.csv");
        System.out.println(String.format("Loaded %d rows in %s ",amount,(System.currentTimeMillis()-start)/1000));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
        ctx.clearCreated();
        fuseClient.shutdown();
    }

    @Test
    public void testFetchPhonePropertiesAndRelationsWithMultiVertices() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match " +
                        " (phone:Entity)-[hasEv1:hasEvalue]->(value1:Evalue {stringValue:'6671870408'})," +
                        " (phone:Entity)-[hasRelEn:relatedEntity]->(loc:Entity), " +
                                                                 " (loc:Entity)-[hasEv2:hasEvalue]->(value2:Evalue), " +
                        " (phone:Entity)-[hasR:hasRelation]->(rel2:Relation), " +
                                                           " (rel2:Relation)-[hasRv:hasRvalue]->(rValue:Rvalue) " +
                        " Where " +
                                " (rValue.fieldId = 'duration' AND rValue.stringValue = 58) AND " +
                                " (loc.category = 'location' AND value2.fieldId = 'location' )    AND " +
                                " (rel2.category = 'type' )  " +
                        " Return *";
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
    public void testFetchPhonePropertiesWithMultiVertices() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match " +
                        " (phone:Entity)-[hasEv:hasEvalue]->(value:Evalue {stringValue:'6671870408'})," +
                        " (phone:Entity)-[hasR:hasRelation]->(rel2:Relation), " +
                        " (rel2:Relation)-[hasRv:hasRvalue]->(rValue:Rvalue) " +
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
    public void testFetchPhoneRelationWithMultiVertices() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity)-[hasRel:hasRelation]->(rel:Relation)-[hasRv:hasRvalue]->(rValue:Rvalue), " +
                        " (phone:Entity)-[rel:hasEvalue]->(value:Evalue {stringValue:'6671870408'}) " +
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
    public void testFetchPhoneWithGeoLocationBoundingBox() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity )-[rel:hasEvalue]->(value:Evalue) where value.geoValue in ['geo_bounds','25,-106.7','24.5,-106.43'] Return *";
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
    public void testFetchPhoneWithGeoLocationDistance() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (phone:Entity )-[rel:hasEvalue]->(value:Evalue) where value.geoValue in ['geo_distance','25,-106.7','32km'] Return *";
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
        String query = "Match (location:Entity)-[rel:relatedEntity]->(any:Entity) Where (any.category = 'location') Return *";
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
