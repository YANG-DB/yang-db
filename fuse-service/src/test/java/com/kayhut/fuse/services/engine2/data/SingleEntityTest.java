package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.fuse.services.engine2.NonRedundantTestSuite;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.jooby.test.JoobyRule;
import org.junit.*;

import java.io.IOException;
import java.util.*;

import static io.restassured.RestAssured.given;

/**
 * Created by Roman on 12/04/2017.
 */
public class SingleEntityTest {

    @ClassRule
    public static JoobyRule createApp() {
        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine2.dev.conf", "m1.dfs.non_redundant"));
    }

    @BeforeClass
    public static void setup() throws Exception {
        fuseClient = new FuseClient("http://localhost:8888/fuse");

        String idField = "id";

        TransportClient client = NonRedundantTestSuite.elasticEmbeddedNode.getClient();

        new ElasticDataPopulator(
                client,
                "person",
                "Person",
                idField,
                () -> createPeople(10)).populate();

        new ElasticDataPopulator(
                client,
                "dragon",
                "Dragon",
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
        testSinglePageResult("People", "Dragons", 1, 1, 1, Optional.empty());
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", 1, 10, 10,
                Optional.of(Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9")));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MoreThanExists() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", 1, 20, 10,
                Optional.of(Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9")));
    }

    @Test
    public void test_DragonsQuery_SingleAssignment() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", 2, 1, 1, Optional.empty());
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", 2, 10, 10,
                Optional.of(Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9")));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MoreThanExists() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", 2, 20, 10,
                Optional.of(Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9")));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize1() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", 1, 1,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize2() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", 1, 2,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize5() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", 1, 5,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize1() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", 2, 1,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize2() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", 2, 2,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize5() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", 2, 5,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }
    //endregion

    //region TestHelper Methods
    protected void testSinglePageResult(
            String queryName,
            String ontologyName,
            int eType,
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

        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());

        Assert.assertEquals(requestedPageSize, pageResourceInfo.getRequestedPageSize());
        Assert.assertEquals(actualPageSize, pageResourceInfo.getActualPageSize());
        Assert.assertEquals(actualPageSize, pageData.getAssignments().size());

        Set<String> ids = new HashSet<>();
        pageData.getAssignments().forEach(assignment -> {
            Assert.assertTrue(assignment.getEntities().size() == 1);
            ids.add(assignment.getEntities().get(0).geteID());

            Assert.assertTrue(assignment.getEntities().get(0).geteTag().size() == 1);
            Assert.assertTrue(assignment.getEntities().get(0).geteTag().get(0).equals("A"));
            Assert.assertTrue(assignment.getEntities().get(0).geteType() == eType);
        });

        if (expectedIds.isPresent()) {
            Assert.assertTrue(ids.size() == expectedIds.get().size());
            Assert.assertTrue(ids.containsAll(expectedIds.get()));
        }
    }

    protected void testMultiplePageResults(
            String queryName,
            String ontologyName,
            int eType,
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

            QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());
            Assert.assertTrue(pageData.getAssignments().size() == pageSize);
            pageData.getAssignments().forEach(assignment -> {
                Assert.assertTrue(assignment.getEntities().size() == 1);
                ids.add(assignment.getEntities().get(0).geteID());

                Assert.assertTrue(assignment.getEntities().get(0).geteTag().size() == 1);
                Assert.assertTrue(assignment.getEntities().get(0).geteTag().get(0).equals("A"));
                Assert.assertTrue(assignment.getEntities().get(0).geteType() == eType);
            });
        }

        Assert.assertTrue(ids.size() == expectedIds.size());
        Assert.assertTrue(ids.containsAll(expectedIds));

        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), pageSize);
        QueryResult pageData = fuseClient.getPageData(pageResourceInfo.getDataUrl());

        Assert.assertTrue(pageResourceInfo.getRequestedPageSize() == pageSize);
        Assert.assertTrue(pageResourceInfo.getActualPageSize() == 0);
        Assert.assertTrue(pageData.getAssignments() == null || pageData.getAssignments().size() == 0);
    }
    //endregion

    //region Protected Methods
    protected Query createSimpleEntityQuery(String queryName, String ontologyName, int entityType) {
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
