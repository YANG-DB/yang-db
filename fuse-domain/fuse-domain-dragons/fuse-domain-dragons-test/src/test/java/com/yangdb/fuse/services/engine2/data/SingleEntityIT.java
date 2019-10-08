package com.yangdb.fuse.services.engine2.data;

import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Relationship;
import com.yangdb.fuse.services.TestsConfiguration;
import com.yangdb.fuse.services.engine2.NonRedundantTestSuite;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.MappingElasticConfigurer;
import com.yangdb.fuse.test.framework.index.Mappings;
import com.yangdb.fuse.test.framework.index.Mappings.Mapping;
import com.yangdb.fuse.test.framework.index.Mappings.Mapping.Property;
import com.yangdb.fuse.test.framework.populator.ElasticDataPopulator;
import com.yangdb.test.BaseITMarker;
import javaslang.collection.Stream;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;

import java.io.IOException;
import java.util.*;

import static com.yangdb.fuse.test.framework.index.Mappings.Mapping.Property.Type.keyword;
import static io.restassured.RestAssured.given;

/**
 * Created by Roman on 12/04/2017.
 */
public class SingleEntityIT implements BaseITMarker {
    @BeforeClass
    public static void setup() throws Exception {
        fuseClient = new BaseFuseClient("http://localhost:8888/fuse");

        String idField = "id";

        TransportClient client = NonRedundantTestSuite.elasticEmbeddedNode.getClient();

        new MappingElasticConfigurer("person", new Mappings().addMapping("pge",
                new Mapping().addProperty("type", new Property(keyword))
                    .addProperty("name", new Property(keyword)))).configure(client);

        new ElasticDataPopulator(
                client,
                "person",
                "pge",
                idField,
                () -> createPeople(10)).populate();


        new MappingElasticConfigurer("dragon", new Mappings().addMapping("pge",
                new Mapping().addProperty("type", new Property(keyword))
                        .addProperty("name", new Property(keyword)))).configure(client);
        new ElasticDataPopulator(
                client,
                "dragon",
                "pge",
                idField,
                () -> createDragons(10)).populate();

        client.admin().indices().refresh(new RefreshRequest("person", "dragon")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        NonRedundantTestSuite.elasticEmbeddedNode.getClient().admin().indices()
                .delete(new DeleteIndexRequest("person", "dragon")).actionGet();
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    //region TestMethods
    @Test
    public void test_PeopleQuery_SingleAssignment() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 1, 1, Optional.empty());
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 10, 10,
                Optional.of(Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9")));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MoreThanExists() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 20, 10,
                Optional.of(Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9")));
    }

    @Test
    public void test_DragonsQuery_SingleAssignment() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", "Dragon", 1, 1, Optional.empty());
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", "Dragon", 10, 10,
                Optional.of(Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9")));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MoreThanExists() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", "Dragon", 20, 10,
                Optional.of(Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9")));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize1() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", "Person", 1,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize2() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", "Person", 2,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize5() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", "Person", 5,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize1() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", "Dragon", 1,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize2() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", "Dragon", 2,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize5() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", "Dragon", 5,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }
    //endregion

    //region TestHelper Methods
    protected void testSinglePageResult(
            String queryName,
            String ontologyName,
            String eType,
            int requestedPageSize,
            int actualPageSize,
            Optional<Collection<String>> expectedIds
    ) throws IOException, InterruptedException {

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), createSimpleEntityQuery(queryName, ontologyName, eType));
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), requestedPageSize);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());

        Assert.assertEquals(requestedPageSize, pageResourceInfo.getRequestedPageSize());
        Assert.assertEquals(actualPageSize, pageResourceInfo.getActualPageSize());
        List<Assignment<Entity,Relationship>> assignments = pageData.getAssignments();
        Assert.assertEquals(actualPageSize, assignments.size());

        Set<String> ids = new HashSet<>();
        assignments.forEach(assignment -> {
            Assert.assertTrue(assignment.getEntities().size() == 1);
            ids.add(assignment.getEntities().get(0).geteID());

            Assert.assertTrue(assignment.getEntities().get(0).geteTag().size() == 1);
            Assert.assertTrue(Stream.ofAll(assignment.getEntities().get(0).geteTag()).get(0).equals("A"));
            Assert.assertTrue(assignment.getEntities().get(0).geteType().equals(eType));
        });

        if (expectedIds.isPresent()) {
            Assert.assertTrue(ids.size() == expectedIds.get().size());
            Assert.assertTrue(ids.containsAll(expectedIds.get()));
        }
    }

    protected void testMultiplePageResults(
            String queryName,
            String ontologyName,
            String eType,
            int pageSize,
            Collection<String> expectedIds
    ) throws IOException, InterruptedException {

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), createSimpleEntityQuery(queryName, ontologyName, eType));
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        Set<String> ids = new HashSet<>();
        for(int i = 0 ; i < (10 / pageSize) ; i++) {
            PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), pageSize);
            while (!pageResourceInfo.isAvailable()) {
                pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
                if (!pageResourceInfo.isAvailable()) {
                    Thread.sleep(10);
                }
            }

            Assert.assertTrue(pageResourceInfo.getRequestedPageSize() == pageSize);
            Assert.assertTrue(pageResourceInfo.getActualPageSize() == pageSize);

            AssignmentsQueryResult pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());
            List<Assignment<Entity,Relationship>> assignments = pageData.getAssignments();
            Assert.assertTrue(assignments.size() == pageSize);
            assignments.forEach(assignment -> {
                Assert.assertTrue(assignment.getEntities().size() == 1);
                ids.add(assignment.getEntities().get(0).geteID());

                Assert.assertTrue(assignment.getEntities().get(0).geteTag().size() == 1);
                Assert.assertTrue(Stream.ofAll(assignment.getEntities().get(0).geteTag()).get(0).equals("A"));
                Assert.assertTrue(assignment.getEntities().get(0).geteType().equals(eType));
            });
        }

        Assert.assertTrue(ids.size() == expectedIds.size());
        Assert.assertTrue(ids.containsAll(expectedIds));

        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), pageSize);
        AssignmentsQueryResult pageData = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());

        Assert.assertTrue(pageResourceInfo.getRequestedPageSize() == pageSize);
        Assert.assertTrue(pageResourceInfo.getActualPageSize() == 0);
        Assert.assertTrue(pageData.getAssignments() == null || pageData.getAssignments().size() == 0);
    }
    //endregion

    //region Protected Methods
    protected Query createSimpleEntityQuery(String queryName, String ontologyName, String entityType) {
        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);

        ETyped eTyped = new ETyped();
        eTyped.seteType(entityType);
        eTyped.seteTag("A");
        eTyped.seteNum(1);

        return Query.Builder.instance()
                .withName(queryName)
                .withOnt(ontologyName)
                .withElements(Arrays.asList(start, eTyped))
                .build();
    }

    protected static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "p" + i);
            person.put("type", "Person");
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }

    protected static Iterable<Map<String, Object>> createDragons(int numDragons) {
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "d" + i);
            dragon.put("type", "Dragon");
            dragon.put("name", "dragon" + i);
            dragons.add(dragon);
        }
        return dragons;
    }
    //endregion

    //region Fields
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static FuseClient fuseClient;
    //endregion
}
