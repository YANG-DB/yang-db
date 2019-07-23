package com.yangdb.fuse.assembly.knowledge.cypher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import javaslang.Tuple3;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeStaticRuleBasedStatisticalProvider.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.nextPage;
import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder._v;
import static java.time.temporal.ChronoField.EPOCH_DAY;

public class KnowledgeMassInsertionPathTest {
    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    public static final String[] words = LOREM_IPSUM.split(" ");

    public static final int ENTITY_COUNT = 1000;
    public static int valuesCount;
    public static int relationCount;

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static KnowledgeWriterContext ctx;
    static Random rand;
    static ObjectMapper mapper;

    @BeforeClass
    public static void setup() throws Exception {
        mapper = new ObjectMapper();
        Setup.setup(true);
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

        final int entitiesCount = commit(ctx.client, INDEX, mapper, entities);
        System.out.println("completed writing " + entitiesCount + " entities");
        valuesCount = commit(ctx.client, INDEX,mapper, values);
        System.out.println("completed writing " + valuesCount + " e.values");
        relationCount = commit(ctx.client, REL_INDEX,mapper, relations);
        System.out.println("completed writing " + relationCount + " relations");
    }

    private static Tuple3<List<EntityBuilder>, List<ValueBuilder>, List<RelationBuilder>> populateEntityWithRelation(int index) {
        String logicalId = ctx.nextLogicalId();
        final EntityBuilder e1 = _e(logicalId).cat(getCategory(index))
                .ctx("context1")
                .techId(logicalId+"_techId")
                .creationTime(getCreationTime(index)).creationUser(getCreationUser(index));

        String logicalId1 = ctx.nextLogicalId();
        final EntityBuilder e2 = _e(logicalId1).cat(getCategory(index))
                .ctx("context1")
                .techId(logicalId1+"_techId")
                .creationTime(getCreationTime(index))
                .creationUser(getCreationUser(index));

        final List<ValueBuilder> valueBuilders =
                IntStream.rangeClosed(1, rand.nextInt(5) + 1)
                .mapToObj(i -> e1.withValue(_v(ctx.nextValueId()).field(getFieldName(index)).value(getFieldValue(index)).bdt("???")
                        .creationTime(getCreationTime(index)).creationUser(getCreationUser(index)))).collect(Collectors.toList());

        String relId = ctx.nextRelId();
        final RelationBuilder rel = _rel(relId)
                .ctx("context1")
                .cat(getCategory(index))
                .techId(relId+"_techId")
                .creationTime(getCreationTime(index))
                .creationUser(getCreationUser(index));

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
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(500)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 500);
        long totalGraphSize = 0;
        long totalEntitiesSize = 0;
        long totalValuesSize = 0;
        while (pageData.getSize() > 0) {
            // Check Entity Response
            final long entityCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                    .flatMap(p -> p.getEntities().stream()).filter(p -> p.geteType().equals("Entity")).count();
            final long valueCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                    .flatMap(p -> p.getEntities().stream()).filter(p -> p.geteType().equals("Evalue")).count();

            if (pageData.getSize() < 500) {
                //last page
                Assert.assertTrue(pageData.getSize() > 0);
            } else {
                Assert.assertEquals(500, pageData.getSize());
                Assert.assertEquals(500, entityCount);
                Assert.assertEquals(500, valueCount);
            }
            totalEntitiesSize +=entityCount;
            totalValuesSize +=valueCount;
            totalGraphSize += pageData.getSize();
            pageData = nextPage(fuseClient, cursorResourceInfo, 500);
        }
        Assert.assertEquals(valuesCount, totalGraphSize);

    }

    @Test
    public void testFetchEntityWithMetadataFilterAndValuesGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity {techId: 'e00000001_techId'})-[r:hasEvalue]->(ev:Evalue) Return *";

        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(500)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        int size = 0;
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 500);
        size+=pageData.getSize();
        while (pageData.getSize() > 0) {
            pageData = nextPage(fuseClient, cursorResourceInfo, 500);
            size+=pageData.getSize();
        }
        Assert.assertTrue(size >= 1);

    }

    @Test
    public void testFetchEntityWithValuesFilterGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) where (ev.stringValue =~'Lorem*') Return *";

        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(500)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 500);
        long totalGraphSize = 0;
        long totalEntitiesSize = 0;
        long totalValuesSize = 0;
        while (pageData.getSize() > 0) {
            // Check Entity Response
            final long entityCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                    .flatMap(p -> p.getEntities().stream()).filter(p -> p.geteType().equals("Entity")).count();
            long valueCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream().flatMap(p -> p.getEntities().stream()).filter(p -> p.geteType().equals("Evalue"))
                    .filter(p -> p.getProperty("stringValue").get().getValue().toString().equals("Lorem"))
                    .count();

            if (pageData.getSize() < 500) {
                //last page
                Assert.assertTrue(pageData.getSize() > 0);
            } else {
                Assert.assertEquals(500, pageData.getSize());
                Assert.assertEquals(500, entityCount);
                Assert.assertEquals(500, valueCount);
            }
            totalEntitiesSize +=entityCount;
            totalValuesSize +=valueCount;
            totalGraphSize += pageData.getSize();
            pageData = nextPage(fuseClient, cursorResourceInfo, 500);
        }
        Assert.assertTrue(totalValuesSize<valuesCount);
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
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        long totalGraphSize = 0;
        long totalEValueRelSize = 0;
        long totalHasRelSize = 0;
        long totalEntitiesSize = 0;
        long totalValuesSize = 0;
        while (pageData.getSize() > 0) {
            // Check Entity Response
            final long entityCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                    .flatMap(p -> p.getEntities().stream()).filter(p -> p.geteType().equals("Entity")).count();
            long valueCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                    .flatMap(p -> p.getEntities().stream()).filter(p -> p.geteType().equals("Evalue"))
                    .count();
            long relEValueCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                    .flatMap(p -> p.getRelationships().stream().filter(r->r.getrType().equals("hasEvalue")))
                    .count();
            long relHasRelCount = ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                    .flatMap(p -> p.getRelationships().stream().filter(r->r.getrType().equals("hasRelation")))
                    .count();

            if (pageData.getSize() < 500) {
                //last page
                Assert.assertTrue(pageData.getSize() > 0);
            } else {
                Assert.assertEquals(500, pageData.getSize());
                Assert.assertEquals(500, entityCount);
                Assert.assertEquals(500, valueCount);
            }
            totalEntitiesSize +=entityCount;
            totalEValueRelSize +=relEValueCount;
            totalHasRelSize +=relHasRelCount;
            totalValuesSize +=valueCount;
            totalGraphSize += pageData.getSize();
            pageData = nextPage(fuseClient, cursorResourceInfo, 500);
        }
        //compare Entity created + EValues + relation ( entity + relEntity + value+hasEvalue )
        Assert.assertEquals(valuesCount , totalEValueRelSize);
    }
    @Test
    public void testFetchEntityWithRelationTechIfFilterSideB_WithValuesGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue)," +
                " (e:Entity)-[r:hasRelation]->(rel:Relation {techId: 'r00000001_techId'}) " +
                " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        int rSize = pageData.getSize();
        while (pageData.getSize() > 0) {
            pageData = nextPage(fuseClient, cursorResourceInfo, 500);
            rSize += pageData.getSize();
        }
        //compare Entity created + EValues + relation ( entity + relEntity + value+hasEvalue )
        Assert.assertTrue(rSize >=1);
    }

    @Test
    public void testFetchEntityWithRelationAndEntityTechIfFilterSideB_WithValuesGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue)," +
                " (e:Entity)-[rIn:hasRelation]->(rel:Relation {techId: 'r00000001_techId'}), " +
                " (rel:Relation {entityBTechId: 'e00000002_techId'})<-[rOut:hasRelation ]-(e2:Entity)" +
                " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        int rSize = pageData.getSize();
        while (pageData.getSize() > 0) {
            pageData = nextPage(fuseClient, cursorResourceInfo, 500);
            rSize += pageData.getSize();
        }
        //compare Entity created + EValues + relation ( entity + relEntity + value+hasEvalue )
        Assert.assertTrue(rSize >=1);
    }

    @Test
    public void testFetchEntityWithRelationAndEntityTechSideB_WithValuesGraph() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue)," +
                " (e:Entity)-[rIn:hasRelation]->(rel:Relation), " +
                " (rel:Relation {entityBTechId: 'e00000002_techId'})<-[rOut:hasRelation ]-(e2:Entity)" +
                " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreatePathsCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        int rSize = pageData.getSize();
        while (pageData.getSize() > 0) {
            pageData = nextPage(fuseClient, cursorResourceInfo, 500);
            rSize += pageData.getSize();
        }
        //compare Entity created + EValues + relation ( entity + relEntity + value+hasEvalue )
        Assert.assertTrue(rSize >=1);
    }
}
