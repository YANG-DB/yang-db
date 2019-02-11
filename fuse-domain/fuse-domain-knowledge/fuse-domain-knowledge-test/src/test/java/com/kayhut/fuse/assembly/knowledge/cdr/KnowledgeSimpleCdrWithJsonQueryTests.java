package com.kayhut.fuse.assembly.knowledge.cdr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.assembly.knowledge.Setup;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.parser.JsonQueryTranslator;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.model.query.quant.QuantType;
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
    public void testSomething() throws IOException, InterruptedException {
        String v1 = "{\n" +
                "   \"ont\":\"Knowledge\",\n" +
                "   \"name\":\"Query:1549821850354\",\n" +
                "   \"elements\":[\n" +
                "      {\n" +
                "         \"type\":\"Start\",\n" +
                "         \"eNum\":0,\n" +
                "         \"next\":1\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"ETyped\",\n" +
                "         \"eNum\":1,\n" +
                "         \"eTag\":\"_1\",\n" +
                "         \"next\":2,\n" +
                "         \"eType\":\"Entity\"\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"Quant1\",\n" +
                "         \"eNum\":2,\n" +
                "         \"qType\":\"all\",\n" +
                "         \"next\":[\n" +
                "            3\n" +
                "         ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"Rel\",\n" +
                "         \"eNum\":3,\n" +
                "         \"rType\":\"hasEvalue\",\n" +
                "         \"dir\":\"R\",\n" +
                "         \"wrapper\":\"3:_hasEvalue_http://huha.com#conceptType\",\n" +
                "         \"next\":4\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"ETyped\",\n" +
                "         \"eNum\":4,\n" +
                "         \"eTag\":\"4:_eValue_http://huha.com#conceptType\",\n" +
                "         \"next\":5,\n" +
                "         \"eType\":\"Evalue\"\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"Quant1\",\n" +
                "         \"eNum\":5,\n" +
                "         \"qType\":\"all\",\n" +
                "         \"next\":[\n" +
                "            6\n" +
                "         ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"EPropGroup\",\n" +
                "         \"eNum\":6,\n" +
                "         \"next\":[\n" +
                "            7,\n" +
                "            8\n" +
                "         ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"EProp\",\n" +
                "         \"eNum\":7,\n" +
                "         \"pType\":\"fieldId\",\n" +
                "         \"con\":{\n" +
                "            \"type\":\"Constraint\",\n" +
                "            \"op\":\"eq\",\n" +
                "            \"expr\":\"http://huha.com#conceptType\",\n" +
                "            \"iType\":\"[]\"\n" +
                "         }\n" +
                "      },\n" +
                "      {\n" +
                "         \"type\":\"EProp\",\n" +
                "         \"eNum\":8,\n" +
                "         \"pType\":\"stringValue\",\n" +
                "         \"con\":{\n" +
                "            \"type\":\"Constraint\",\n" +
                "            \"op\":\"eq\",\n" +
                "            \"expr\":\"concept\",\n" +
                "            \"iType\":\"[]\"\n" +
                "         }\n" +
                "      }\n" +
                "   ]\n" +
                "}\n";

        com.kayhut.fuse.model.query.Query q1 = com.kayhut.fuse.model.query.Query.Builder.instance().withName("Query" + System.currentTimeMillis()).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A1", "Entity", 3, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3),-1),
                        new Rel(3, "hasEvalue", R, null, 4, 0),
                        new ETyped(4, "A2", "Evalue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6),-1),
                        new EPropGroup(6,
                            new EProp(7, "fieldId", Constraint.of(ConstraintOp.eq,"http://huha.com#conceptType")),
                            new EProp(8, "stringValue", Constraint.of(ConstraintOp.eq,"concept")))
                )).build();

        Query query = new ObjectMapper().readValue(v1, Query.class);
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), q1);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 10);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        // return the relevant data
        QueryResultBase pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(pageData);
        Assert.assertEquals(1,pageData.getSize());

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
