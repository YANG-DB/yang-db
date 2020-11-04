package com.yangdb.fuse.assembly.knowledge.cypher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import javaslang.Tuple3;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.yangdb.fuse.client.FuseClientSupport.countGraphElements;
import static com.yangdb.fuse.client.FuseClientSupport.nextPage;
import static java.time.temporal.ChronoField.EPOCH_DAY;

@Ignore("Remove random to disable non predictive assert result numbers")
public class KnowledgeMassInsertionGraphTest {
    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    public static final String[] words = LOREM_IPSUM.split(" ");

    public static final int ENTITY_COUNT = 1000;
    public static int valuesCount;
    public static  int relationCount;

    static SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    static KnowledgeWriterContext ctx;
    static Random rand;

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(true); //todo remove remark when running IT tests
        rand = new Random();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        //load data
        loadData();
    }

    private static void loadData() throws JsonProcessingException {
        List<EntityBuilder> entities = new ArrayList<>();
        List<ValueBuilder> values = new ArrayList<>();
        List<RelationBuilder> relations = new ArrayList<>();

        IntStream.rangeClosed(1, ENTITY_COUNT).forEach(index -> {
            final Tuple3<List<EntityBuilder>, List<ValueBuilder>, List<RelationBuilder>> tuple3 = populateEntityWithRelation(index);
            entities.addAll(tuple3._1);
            values.addAll(tuple3._2);
            relations.addAll(tuple3._3);
        });

        final int entitiesCount = commit(ctx, INDEX, entities);
        System.out.println("completed writing " + entitiesCount + " entities");
        valuesCount = commit(ctx, INDEX, values);
        System.out.println("completed writing " + valuesCount + " e.values");
        relationCount = commit(ctx, REL_INDEX, relations);
        System.out.println("completed writing " + relationCount + " relations");
    }

    private static Tuple3<List<EntityBuilder>, List<ValueBuilder>, List<RelationBuilder>> populateEntityWithRelation(int index) {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat(getCategory(index)).ctx("context1")
                .creationTime(getCreationTime(index)).creationUser(getCreationUser(index));

        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat(getCategory(index)).ctx("context1")
                .creationTime(getCreationTime(index)).creationUser(getCreationUser(index));

        final List<ValueBuilder> valueBuilders = IntStream.rangeClosed(1, rand.nextInt(5) + 1)
                .mapToObj(i -> e1.withValue(_v(ctx.nextValueId()).field(getFieldName(index)).value(getFieldValue(index)).bdt("???")
                        .creationTime(getCreationTime(index)).creationUser(getCreationUser(index)))).collect(Collectors.toList());

        final RelationBuilder rel = _rel(ctx.nextRelId()).ctx("context1").cat(getCategory(index)).creationUser(getCreationUser(index))
                .creationTime(getCreationTime(index)).creationUser(getCreationUser(index));

        rel.sideA(e1).sideB(e2);
        e1.rel(rel, "out");
        e2.rel(rel, "in");

        return new Tuple3<>(Arrays.asList(e1, e2), valueBuilders, Collections.singletonList(rel));
    }

    private static String getFieldName(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private static Object getFieldValue(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private static String getCreationUser(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private static String getCategory(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private static Date getCreationTime(int index) {
        int minDay = (int) LocalDate.of(1900, 1, 1).toEpochDay();
        int maxDay = (int) LocalDate.of(2019, 1, 1).toEpochDay();
        long randomDay = minDay + rand.nextInt(maxDay - minDay);

        return new Date(LocalDate.ofEpochDay(randomDay).getLong(EPOCH_DAY));
    }

    @Test
    public void testFetchEntityWithValuesGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            Assert.assertEquals(1, pageData.getSize());
            Assert.assertEquals(1, assignments.size());
            Assert.assertEquals(false, assignments.get(0).getRelationships().isEmpty());
            Assert.assertEquals(false, assignments.get(0).getEntities().isEmpty());

            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        //compare Entity created + EValues * 2 ( include the hasEvalue rel per each EValue)
        Assert.assertEquals(7128, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithValuesFilterGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) where (ev.stringValue =~'Lorem*') Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            Assert.assertEquals(1, pageData.getSize());
            Assert.assertEquals(1, assignments.size());
            Assert.assertEquals(false, assignments.get(0).getRelationships().isEmpty());
            Assert.assertEquals(false, assignments.get(0).getEntities().isEmpty());

            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        //compare Entity created + EValues * 2 ( include the hasEvalue rel per each EValue)
        long eValues = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream().flatMap(p -> p.getEntities().stream()).filter(p -> p.geteType().equals("Evalue"))
                .filter(p -> p.getProperty("stringValue").get().getValue().toString().equals("Lorem"))
                .count();
        long entities = countGraphElements(pageData,false,true,relationship -> false,entity -> true);
        Assert.assertEquals(entities+eValues, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithRelationAndValuesGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue)," +
                        " (e:Entity)-[r:hasRelation]->(rel:Relation) " +
                        " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            Assert.assertEquals(1, pageData.getSize());
            Assert.assertEquals(1, assignments.size());
            Assert.assertEquals(false, assignments.get(0).getRelationships().isEmpty());
            Assert.assertEquals(false, assignments.get(0).getEntities().isEmpty());

            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        //compare Entity created + EValues + relation ( entity + relEntity + value+hasEvalue )
        Assert.assertEquals(10013, totalGraphSize);
    }

    @Test
    public void testFetchEntityWithRelationGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasRelation]->(rel:Relation) Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            Assert.assertEquals(1, pageData.getSize());
            Assert.assertEquals(1, assignments.size());
            Assert.assertEquals(false, assignments.get(0).getRelationships().isEmpty());
            Assert.assertEquals(false, assignments.get(0).getEntities().isEmpty());

            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(6041, totalGraphSize);
    }


}
