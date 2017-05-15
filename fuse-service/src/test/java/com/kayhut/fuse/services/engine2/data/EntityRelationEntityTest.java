package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.test.framework.index.ElasticInMemoryIndex;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.collections.map.HashedMap;
import org.jooby.test.JoobyRule;
import org.junit.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Roman on 11/05/2017.
 */
public class EntityRelationEntityTest {
    @ClassRule
    public static JoobyRule createApp() {
        return new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf("application.engine2.dev.conf"));
    }

    @BeforeClass
    public static void setup() throws Exception {
        fuseClient = new FuseClient("/fuse");

        String idField = "id";

        elasticInMemoryIndex = new ElasticInMemoryIndex();
        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                "person",
                "Person",
                idField,
                () -> createPeople(10)).populate();

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                "dragon",
                "Dragon",
                idField,
                () -> createDragons(10)).populate();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                "fire20170511",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(sdf.parse("2017-05-11"), 1200000, 10)).populate(); // date interval is 20 min

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                "fire20170512",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(sdf.parse("2017-05-12"), 600000, 10)).populate(); // date interval is 10 min

        new ElasticDataPopulator(
                elasticInMemoryIndex.getClient(),
                "fire20170513",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(sdf.parse("2017-05-13"), 300000, 10)).populate(); // date interval is 5 min


        Thread.sleep(2000);
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    //region Tests
    @Test
    @Ignore
    public void test_dragon_fire_dragon() throws IOException, InterruptedException {
        Query query = Query.QueryBuilder.aQuery().withName("name").withOnt("Dragons").withElements(Arrays.asList(
                new Start(0, 1), new ETyped(1, "A", 1, 2, 0), new Rel(2, 1, Rel.Direction.R, null, 3, 0), new ETyped(3, "B", 1, 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(100);
            }
        }

        int x = 5;
    }
    //endregion

    //region Protected Methods

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

    protected static Iterable<Map<String, Object>> createDragonFireDragonEdges(Date startingDate, long dateInterval, int numDragons) throws ParseException {
        List<Map<String, Object>> fireEdges = new ArrayList<>();

        long currentDate = startingDate.getTime();
        int counter = 0;
        for(int i = 0 ; i < numDragons ; i++) {
            for(int j = 0 ; j < i ; j++) {
                Map<String, Object> fireEdge = new HashMap<>();
                fireEdge.put("id", "fire" + counter++);
                fireEdge.put("timestamp", currentDate);
                fireEdge.put("direction", "out");

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", "fire" + counter++);
                fireEdgeDual.put("timestamp", currentDate);
                fireEdgeDual.put("direction", "in");

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "d" + i);
                entityAI.put("type", "Dragon");
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "d" + j);
                entityAJ.put("type", "Dragon");
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "d" + i);
                entityBI.put("type", "Dragon");
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "d" + j);
                entityBJ.put("type", "Dragon");

                fireEdge.put("entityA", entityAI);
                fireEdge.put("entityB", entityBJ);
                fireEdgeDual.put("entityA", entityAJ);
                fireEdgeDual.put("entityB", entityBI);

                fireEdges.addAll(Arrays.asList(fireEdge, fireEdgeDual));

                currentDate += dateInterval;
            }
        }

        return fireEdges;
    }
    //endregion

    //region Fields
    private static ElasticInMemoryIndex elasticInMemoryIndex;
    private static FuseClient fuseClient;
    //endregion
}
