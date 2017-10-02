package com.kayhut.fuse.services.engine2.discrete;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
@Ignore
public class SingleEntityTest {
    //region setup
    @BeforeClass
    public static void setup() throws Exception {
        fuseClient = new FuseClient("http://localhost:8888/fuse");

        String idField = "id";

        TransportClient client = RedundantTestSuite.elasticEmbeddedNode.getClient();

        new ElasticDataPopulator(
                client,
                "person1",
                "Person",
                idField,
                true,
                null,
                false,
                () -> createPeople(0, 5)).populate();

        new ElasticDataPopulator(
                client,
                "person2",
                "Person",
                idField,
                true,
                null,
                false,
                () -> createPeople(5, 10)).populate();

        client.admin().indices().refresh(new RefreshRequest("person1", "person2")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        RedundantTestSuite.elasticEmbeddedNode.getClient().admin().indices()
                .delete(new DeleteIndexRequest("person1", "person2")).actionGet();
    }
    //endregion

    //region Tests
    @Test
    public void test_PeopleQuery_SingleAssignment() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 1, 1, Optional.empty());
    }
    //endregion

    //region Protected Methods
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
            Assert.assertTrue(assignment.getEntities().get(0).geteType().equals(eType));
        });

        if (expectedIds.isPresent()) {
            Assert.assertTrue(ids.size() == expectedIds.get().size());
            Assert.assertTrue(ids.containsAll(expectedIds.get()));
        }
    }

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

    protected static Iterable<Map<String, Object>> createPeople(int startId, int endId) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = startId ; i < endId ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "p" + String.format("%03d", i));
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }
    //endregion

    //region Fields
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static FuseClient fuseClient;
    //endregion
}
