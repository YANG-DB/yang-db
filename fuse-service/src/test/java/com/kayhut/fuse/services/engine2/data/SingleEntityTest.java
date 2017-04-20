package com.kayhut.fuse.services.engine2.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.test.framework.index.ElasticInMemoryIndex;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.collections.map.HashedMap;
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
                .conf("application.engine2.dev.conf"));
    }

    @BeforeClass
    public static void setup() throws Exception {
        String indexName = "vertices";
        String typeName = "Dragon";
        String idField = "id";

        ElasticInMemoryIndex elasticInMemoryIndex = new ElasticInMemoryIndex();
        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                indexName,
                "Person",
                idField,
                () -> createPeople(10)).populate();

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                indexName,
                "Dragon",
                idField,
                () -> createDragons(10)).populate();

        Thread.sleep(2000);
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

        QueryResourceInfo queryResourceInfo = postQuery(createSimpleEntityQuery(queryName, ontologyName, eType));
        CursorResourceInfo cursorResourceInfo = postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = postPage(cursorResourceInfo.getPageStoreUrl(), requestedPageSize);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(100);
            }
        }

        QueryResult pageData = getPageData(pageResourceInfo.getDataUrl());

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

        QueryResourceInfo queryResourceInfo = postQuery(createSimpleEntityQuery(queryName, ontologyName, eType));
        CursorResourceInfo cursorResourceInfo = postCursor(queryResourceInfo.getCursorStoreUrl());

        Set<String> ids = new HashSet<>();
        for(int i = 0 ; i < (10 / pageSize) ; i++) {
            PageResourceInfo pageResourceInfo = postPage(cursorResourceInfo.getPageStoreUrl(), pageSize);
            while (!pageResourceInfo.isAvailable()) {
                pageResourceInfo = getPage(pageResourceInfo.getResourceUrl());
                if (!pageResourceInfo.isAvailable()) {
                    Thread.sleep(100);
                }
            }

            Assert.assertTrue(pageResourceInfo.getRequestedPageSize() == pageSize);
            Assert.assertTrue(pageResourceInfo.getActualPageSize() == pageSize);

            QueryResult pageData = getPageData(pageResourceInfo.getDataUrl());
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

        PageResourceInfo pageResourceInfo = postPage(cursorResourceInfo.getPageStoreUrl(), pageSize);
        QueryResult pageData = getPageData(pageResourceInfo.getDataUrl());

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

        return Query.QueryBuilder.aQuery()
                .withName(queryName)
                .withOnt(ontologyName)
                .withElements(Arrays.asList(start, eTyped))
                .build();
    }

    protected QueryResourceInfo postQuery(Query query) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        return new ObjectMapper().readValue(unwrap(postRequest(this.appUrlSupplier.queryStoreUrl(), request)), QueryResourceInfo.class);
    }

    protected CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException {
        CreateCursorRequest request = new CreateCursorRequest();
        request.setCursorType(CreateCursorRequest.CursorType.paths);

        return new ObjectMapper().readValue(unwrap(postRequest(cursorStoreUrl, request)), CursorResourceInfo.class);
    }

    protected PageResourceInfo postPage(String pageStoreUrl, int pageSize) throws IOException {
        CreatePageRequest request = new CreatePageRequest();
        request.setPageSize(pageSize);

        return new ObjectMapper().readValue(unwrap(postRequest(pageStoreUrl, request)), PageResourceInfo.class);
    }

    protected PageResourceInfo getPage(String pageUrl) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(pageUrl)), PageResourceInfo.class);
    }

    protected QueryResult getPageData(String pageDataUrl) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(pageDataUrl)), QueryResult.class);
    }

    protected String postRequest(String url, Object body) throws IOException {
        return given().contentType("application/json")
                .body(body)
                .post(url)
                .thenReturn()
                .print();
    }

    protected String getRequest(String url) {
        return given().contentType("application/json")
                .get(url)
                .thenReturn()
                .print();
    }

    protected String unwrap(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>(){});
        return mapper.writeValueAsString(responseMap.get("data"));
    }

    protected static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashedMap();
            person.put("id", "p" + Integer.toString(i));
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }

    protected static Iterable<Map<String, Object>> createDragons(int numDragons) {
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", "d" + Integer.toString(i));
            dragon.put("name", "dragon" + i);
            dragons.add(dragon);
        }
        return dragons;
    }
    //endregion

    //region Fields
    private AppUrlSupplier appUrlSupplier = new DefaultAppUrlSupplier("/fuse");
    //endregion
}
