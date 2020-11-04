package com.yangdb.fuse.services.engine2.discrete;

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
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Relationship;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.MappingElasticConfigurer;
import com.yangdb.fuse.test.framework.index.Mappings;
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

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class SingleEntityIT implements BaseITMarker {
    //region setup
    @BeforeClass
    public static void setup() throws Exception {
        fuseClient = new BaseFuseClient("http://localhost:8888/fuse");

        String idField = "id";

        TransportClient client = ElasticEmbeddedNode.getClient();

        new MappingElasticConfigurer(Arrays.asList("person1", "person2"), new Mappings().addMapping("pge",
                new Mappings.Mapping().addProperty("type", new Mappings.Mapping.Property(keyword))
                        .addProperty("name", new Mappings.Mapping.Property(keyword)))).configure(client);

        new ElasticDataPopulator(
                client,
                "person1",
                "pge",
                idField,
                true,
                null,
                false,
                () -> createPeople(0, 5)).populate();

        new ElasticDataPopulator(
                client,
                "person2",
                "pge",
                idField,
                true,
                null,
                false,
                () -> createPeople(5, 10)).populate();

        client.admin().indices().refresh(new RefreshRequest("person1", "person2")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        ElasticEmbeddedNode.getClient().admin().indices()
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
            person.put("type", "Person");
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }
    //endregion

    //region Fields
    private static FuseClient fuseClient;
    //endregion
}
