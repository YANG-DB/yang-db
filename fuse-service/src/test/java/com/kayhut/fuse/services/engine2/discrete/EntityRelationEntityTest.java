package com.kayhut.fuse.services.engine2.discrete;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.FIRE;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static java.util.Collections.singletonList;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class EntityRelationEntityTest {
    //region setup
    @BeforeClass
    public static void setup() throws Exception {
        fuseClient = new FuseClient("http://localhost:8888/fuse");

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));

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

        new ElasticDataPopulator(
                client,
                "dragon1",
                "Dragon",
                idField,
                true,
                "personId",
                false,
                () -> createDragons(0, 5, 3)).populate();

        new ElasticDataPopulator(
                client,
                "dragon2",
                "Dragon",
                idField,
                true,
                "personId",
                false,
                () -> createDragons(5, 10, 3)).populate();

        client.admin().indices().refresh(new RefreshRequest("person1", "person2", "dragon1", "dragon2")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        RedundantTestSuite.elasticEmbeddedNode.getClient().admin().indices()
                .delete(new DeleteIndexRequest("person1", "person2", "dragon1", "dragon2")).actionGet();
    }
    //endregion

    //region Tests
    @Test
    public void test_Person_own_Dragon() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, singletonList(NAME.type), 2, 0),
                new Rel(2, OWN.getrType(), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", DRAGON.type, singletonList(NAME.type), 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test_person1_own_Dragon() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, singletonList(NAME.type), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, NAME.type, Constraint.of(ConstraintOp.eq, "person1")),
                new Rel(4, OWN.getrType(), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", DRAGON.type, singletonList(NAME.type), 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }
    //endregion

    //region Protected Methods
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

    protected static Iterable<Map<String, Object>> createDragons(int personStartId, int personEndId, int numDragonsPerPerson) {
        int dragonId = personStartId * numDragonsPerPerson;

        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = personStartId ; i < personEndId ; i++) {
            for (int j = 0; j < numDragonsPerPerson; j++) {
                Map<String, Object> dragon = new HashMap<>();
                dragon.put("id", "d" + String.format("%03d", dragonId));
                dragon.put("personId", "p" + String.format("%03d", i));
                dragon.put("name", "dragon" + dragonId);
                dragons.add(dragon);

                dragonId++;
            }
        }

        return dragons;
    }
    //endregion

    //region Fields
    private static FuseClient fuseClient;
    private static Ontology.Accessor $ont;
    //endregion
}
