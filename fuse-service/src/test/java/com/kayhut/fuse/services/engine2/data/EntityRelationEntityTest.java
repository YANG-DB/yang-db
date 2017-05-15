package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.idProvider.PromiseEdgeIdProvider;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.jooby.test.JoobyRule;
import org.junit.*;

import java.io.File;
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
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/ontology/Dragons"));

        String idField = "id";

        elasticEmbeddedNode = new ElasticEmbeddedNode(
                new MappingFileElasticConfigurer("person", new File("./src/test/resources/mappings/person.mapping.json").getAbsolutePath()),
                new MappingFileElasticConfigurer("dragon", new File("./src/test/resources/mappings/dragon.mapping.json").getAbsolutePath()),
                new MappingFileElasticConfigurer(Arrays.asList("fire20170511", "fire20170512", "fire20170513"),
                        new File("./src/test/resources/mappings/fire.mapping.json").getAbsolutePath())
        );

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "person",
                "Person",
                idField,
                () -> createPeople(10)).populate();

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "dragon",
                "Dragon",
                idField,
                () -> createDragons(10)).populate();

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "fire20170511",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(sdf.parse("2017-05-11"), 1200000, 10)).populate(); // date interval is 20 min

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "fire20170512",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(sdf.parse("2017-05-12"), 600000, 10)).populate(); // date interval is 10 min

        new ElasticDataPopulator(
                elasticEmbeddedNode.getClient(),
                "fire20170513",
                "Fire",
                idField,
                () -> createDragonFireDragonEdges(sdf.parse("2017-05-13"), 300000, 10)).populate(); // date interval is 5 min


        Thread.sleep(2000);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (elasticEmbeddedNode != null) {
            elasticEmbeddedNode.close();
            Thread.sleep(2000);
        }
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    //region Tests
    @Test
    public void test_Dragon_Fire_Dragon() throws Exception {
        Query query = Query.QueryBuilder.aQuery().withName("name").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Dragon"), 2, 0),
                new Rel(2, $ont.rType$("Fire"), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$("Dragon"), 0, 0)
        )).build();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(100);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        QueryResult expectedQueryResult = queryResult_Dragons_Fire_Dragon(10);

        QueryResultAssert.assertEquals(expectedQueryResult, actualQueryResult);
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
                fireEdge.put("direction", Direction.OUT);

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", "fire" + counter++);
                fireEdgeDual.put("timestamp", currentDate);
                fireEdgeDual.put("direction", Direction.IN);

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

    protected static QueryResult queryResult_Dragons_Fire_Dragon(int numDragons) throws Exception {
        QueryResult.Builder builder = QueryResult.Builder.instance();
        PromiseEdgeIdProvider edgeIdProvider = new PromiseEdgeIdProvider(
                Optional.of(Constraint.by(
                        __.and(
                            __.has(T.label, "Fire"),
                            __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT)))));

        for(int i = 0 ; i < numDragons ; i++) {
            for (int j = 0; j < i; j++) {
                Entity entityA = Entity.Builder.instance()
                        .withEID("d" + i)
                        .withETag(Collections.singletonList("A"))
                        .withEType($ont.eType$("Dragon"))
                        .build();

                Entity entityB = Entity.Builder.instance()
                        .withEID("d" + j)
                        .withETag(Collections.singletonList("B"))
                        .withEType($ont.eType$("Dragon"))
                        .build();

                Relationship relationship = Relationship.Builder.instance()
                        .withRID(edgeIdProvider.get(GlobalConstants.Labels.PROMISE,
                                new PromiseVertex(Promise.as(entityA.geteID()), Optional.empty(), null),
                                new PromiseVertex(Promise.as(entityB.geteID()), Optional.empty(), null),
                                null))
                        .withDirectional(true)
                        .withEID1(entityA.geteID())
                        .withEID2(entityB.geteID())
                        .withETag1(entityA.geteTag().get(0))
                        .withETag2(entityB.geteTag().get(0))
                        .withRType($ont.rType$("Fire"))
                        .build();

                builder.withAssignment(Assignment.Builder.instance()
                        .withEntity(entityA)
                        .withEntity(entityB)
                        .withRelationship(relationship).build());
            }
        }

        return builder.build();
    }
    //endregion

    //region Fields
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static FuseClient fuseClient;
    private static Ontology.Accessor $ont;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    //endregion
}
