package com.kayhut.fuse.assembly.knowledge.cdr;

import com.kayhut.fuse.assembly.knowledge.Setup;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.parser.JsonQueryTranslator;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultBase;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

public class KnowledgeSimpleCdrWithJsonQueryTests {
    static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(false,false);
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        long start = System.currentTimeMillis();
        long amount = DataLoader.load( ctx, "./data/cdr-small.csv");
        System.out.println(String.format("Loaded %d rows in %s ",amount,(System.currentTimeMillis()-start)/1000));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
        ctx.clearCreated();
    }

    @Test
    public void testInnerQuery() throws IOException, InterruptedException {
        com.kayhut.fuse.model.query.Query q1 = com.kayhut.fuse.model.query.Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "fieldId", Constraint.of(ConstraintOp.eq,"Lorem"))
                )).build();

        com.kayhut.fuse.model.query.Query q0 = com.kayhut.fuse.model.query.Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 2, 0),
                        new Rel(2, "hasEvalue", R, null, 3, 0),
                        new ETyped(3, "A2", "Evalue", 4, 0),
                        new EProp(4, "stringValue", InnerQueryConstraint.of(ConstraintOp.inSet,q1,"A1.id"))
                )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), q0);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        // return the relevant data
        QueryResultBase pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(pageData);
        Assert.assertEquals(1000,pageData.getSize());
    }


    @Test
    public void testFetchPhonePropertiesAndRelationsWithMultiVertices() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/multiStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = JsonQueryTranslator.jsonParser(new JSONObject(content));

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
