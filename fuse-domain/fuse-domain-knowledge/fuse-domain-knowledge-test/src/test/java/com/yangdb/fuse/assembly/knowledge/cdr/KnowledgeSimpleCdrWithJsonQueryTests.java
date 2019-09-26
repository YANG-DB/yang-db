package com.yangdb.fuse.assembly.knowledge.cdr;

import com.yangdb.fuse.assembly.knowledge.parser.FolderBasedTypeProvider;
import com.yangdb.fuse.assembly.knowledge.parser.JsonQueryTranslator;
import com.yangdb.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultBase;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.yangdb.fuse.assembly.knowledge.Setup.fuseClient;
import static com.yangdb.fuse.client.FuseClientSupport.query;

public class KnowledgeSimpleCdrWithJsonQueryTests {
    static JsonQueryTranslator translator;
    static BusinessTypesProvider typesProvider;

    @BeforeClass
    public static void setup() throws Exception {
        translator = new JsonQueryTranslator();
        typesProvider = new FolderBasedTypeProvider("ontology");
    }


    @Test
    public void testFetchPhonePropertiesAndRelationsWithMultiVertices() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/multiStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = translator.translate(new JSONObject(content),typesProvider);

        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }

    @Test
    public void testFetchPhonePropertiesAndRelationsWithMultiVerticesNoProps() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/multiStepQueryNoProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = translator.translate(new JSONObject(content),typesProvider);

        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }
    @Test
    public void testFetchPropertiesAndRelationsWithSingleEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/singleEntityQueryWithRel.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = translator.translate(new JSONObject(content),typesProvider);

        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }
    @Test
    public void testFetchPropertiesWithSingleEntity() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/singleEntityQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = translator.translate(new JSONObject(content),typesProvider);

        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);
        List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        // Check Entity Response
        Assert.assertEquals(1, assignments.size());

        Assert.assertFalse( assignments.get(0).getEntities().isEmpty() );
        System.out.println("Entities fetched: "+assignments.get(0).getEntities().size());

        Assert.assertFalse( assignments.get(0).getRelationships().isEmpty() );
        System.out.println("Values fetched: "+assignments.get(0).getRelationships().size());

    }

}
