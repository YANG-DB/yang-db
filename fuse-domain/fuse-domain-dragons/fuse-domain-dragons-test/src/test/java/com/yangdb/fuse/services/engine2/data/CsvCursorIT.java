package com.yangdb.fuse.services.engine2.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest;
import com.yangdb.fuse.services.TestsConfiguration;
import com.yangdb.fuse.services.engine2.CsvCursorTestSuite;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.stat.StatCalculator;
import com.yangdb.fuse.stat.configuration.StatConfiguration;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.MappingElasticConfigurer;
import com.yangdb.fuse.test.framework.index.MappingFileElasticConfigurer;
import com.yangdb.fuse.test.framework.index.Mappings;
import com.yangdb.fuse.test.framework.populator.ElasticDataPopulator;
import com.yangdb.test.BaseITMarker;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.client.transport.TransportClient;
import org.junit.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static com.yangdb.fuse.model.OntologyTestUtils.ORIGINATED_IN;
import static com.yangdb.test.data.DragonsOntology.*;

public class CsvCursorIT implements BaseITMarker {
    @BeforeClass
    public static void setup() throws Exception {
        setup(ElasticEmbeddedNode.getClient(), true);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        cleanup(ElasticEmbeddedNode.getClient());
    }


    public static void setup(TransportClient client, boolean calcStats) throws Exception {
        fuseClient = new BaseFuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));

        String idField = "id";

        new MappingElasticConfigurer(PERSON.name.toLowerCase(), new Mappings().addMapping("pge", getPersonMapping()))
                .configure(client);
        new MappingElasticConfigurer(DRAGON.name.toLowerCase(), new Mappings().addMapping("pge", getDragonMapping()))
                .configure(client);
        new MappingElasticConfigurer(KINGDOM.name.toLowerCase(), new Mappings().addMapping("pge", getKingdomMapping()))
                .configure(client);
        new MappingElasticConfigurer(Arrays.asList(
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513"),
                new Mappings().addMapping("pge", getFireMapping()))
                .configure(client);
        new MappingElasticConfigurer(Arrays.asList("originated_in"), new Mappings().addMapping("pge", getOriginMapping()))
                .configure(client);

        birthDateValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        timestampValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        temperatureValueFunction = i -> 1000 + (100 * i);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        int numPersons = 100;
        int numDragons = 100;
        int numKingdoms = 2;

        new ElasticDataPopulator(
                client,
                PERSON.name.toLowerCase(),
                "pge",
                idField,
                () -> createPeople(numPersons)).populate();

        new ElasticDataPopulator(
                client,
                DRAGON.name.toLowerCase(),
                "pge",
                idField,
                () -> createDragons(numDragons, birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L)))
                .populate(); // date interval is ~ 1 month

        new ElasticDataPopulator(client,
                KINGDOM.name.toLowerCase(),
                "pge",
                idField,
                ()-> createKingdoms(numKingdoms))
                .populate();

        new ElasticDataPopulator(client,
                "originated_in",
                "pge",
                idField,
                () -> createOriginEdges(numDragons, numKingdoms)
                ).populate();

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170511",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        numDragons,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-11 00:00:00").getTime()).apply(1200000L),
                        temperatureValueFunction))
                .populate(); // date interval is 20 min

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170512",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        numDragons,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-12 00:00:00").getTime()).apply(600000L),
                        temperatureValueFunction))
                .populate(); // date interval is 10 min

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170513",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        numDragons,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-13 00:00:00").getTime()).apply(300000L),
                        temperatureValueFunction))
                .populate(); // date interval is 5 min


        client.admin().indices().refresh(new RefreshRequest(
                PERSON.name.toLowerCase(),
                DRAGON.name.toLowerCase(),
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513",
                KINGDOM.name.toLowerCase(),
                "originated_in"
        )).actionGet();

        if(calcStats){
            new MappingFileElasticConfigurer("stat", "src/test/resources/stat_mappings.json").configure(client);
            Configuration statConfig = new StatConfiguration("statistics.test.properties").getInstance();
            StatCalculator.run(client, client, statConfig);
            client.admin().indices().refresh(new RefreshRequest("stat")).actionGet();
        }
    }




    public static void cleanup(TransportClient client) throws Exception {
        cleanup(client, true);
    }

    public static void cleanup(TransportClient client, boolean statsUsed) throws Exception {
        client.admin().indices()
                .delete(new DeleteIndexRequest(
                        PERSON.name.toLowerCase(),
                        DRAGON.name.toLowerCase(),
                        FIRE.getName().toLowerCase() + "20170511",
                        FIRE.getName().toLowerCase() + "20170512",
                        FIRE.getName().toLowerCase() + "20170513",
                        KINGDOM.name.toLowerCase(),
                        "originated_in"))
                .actionGet();

        if(statsUsed){
            client.admin().indices().delete(new DeleteIndexRequest("stat")).actionGet();
        }
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    private static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "Person_" + i);
            person.put("type", "Person");
            person.put(NAME.name, "person" + i);
            people.add(person);
        }
        return people;
    }

    private static Mappings.Mapping getPersonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword));
    }

    private static Mappings.Mapping getKingdomMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword));
    }

    private static Iterable<Map<String, Object>> createDragons(
            int numDragons,
            Function<Integer, Long> birthDateValueFunction) {

        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "Dragon_" + i);
            dragon.put("type", DRAGON.name);
            int nameChar = i%58 + 65;
            dragon.put(NAME.name, (char)nameChar);
            dragon.put(BIRTH_DATE.name, sdf.format(new Date(birthDateValueFunction.apply(i))));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Mappings.Mapping getDragonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(BIRTH_DATE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"));
    }


    private static Iterable<Map<String, Object>> createDragonFireDragonEdges(
            int numDragons,
            Function<Integer, Long> timestampValueFunction,
            Function<Integer, Integer> temperatureValueFunction
    ) throws ParseException {
        List<Map<String, Object>> fireEdges = new ArrayList<>();

        int counter = 0;
        for(int i = 0 ; i < numDragons ; i++) {
            for(int j = 0 ; j < i ; j++) {
                Map<String, Object> fireEdge = new HashMap<>();
                fireEdge.put("id", FIRE.getName() + counter);
                fireEdge.put("type", FIRE.getName());
                fireEdge.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdge.put(GlobalConstants.EdgeSchema.DIRECTION, Direction.OUT.name());
                fireEdge.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", FIRE.getName() + counter + 1);
                fireEdgeDual.put("type", FIRE.getName());
                fireEdgeDual.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdgeDual.put(GlobalConstants.EdgeSchema.DIRECTION, Direction.IN.name());
                fireEdgeDual.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "Dragon_" + i);
                entityAI.put("type", DRAGON.name);
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "Dragon_" + j);
                entityAJ.put("type", DRAGON.name);
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "Dragon_" + i);
                entityBI.put("type", DRAGON.name);
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "Dragon_" + j);
                entityBJ.put("type", DRAGON.name);

                fireEdge.put(GlobalConstants.EdgeSchema.SOURCE, entityAI);
                fireEdge.put(GlobalConstants.EdgeSchema.DEST, entityBJ);
                fireEdgeDual.put(GlobalConstants.EdgeSchema.SOURCE, entityAJ);
                fireEdgeDual.put(GlobalConstants.EdgeSchema.DEST, entityBI);

                fireEdges.addAll(Arrays.asList(fireEdge, fireEdgeDual));

                counter += 2;
            }
        }

        return fireEdges;
    }

    private static Mappings.Mapping getFireMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TIMESTAMP.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"))
                .addProperty(GlobalConstants.EdgeSchema.DIRECTION, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TEMPERATURE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.integer))
                .addProperty(GlobalConstants.EdgeSchema.SOURCE, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
                .addProperty(GlobalConstants.EdgeSchema.DEST, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }

    private static Iterable<Map<String, Object>> createKingdoms(int numKingdoms) {
        List<Map<String, Object>> kingdoms = new ArrayList<>();
        for(int i = 0 ; i < numKingdoms ; i++) {
            Map<String, Object> kingdom = new HashMap<>();
            kingdom.put("id", "Kingdom_" + i);
            kingdom.put("type", "Kingdom");
            kingdom.put(NAME.name, "kingdom" + i);
            kingdoms.add(kingdom);
        }
        return kingdoms;
    }

    private static Mappings.Mapping getOriginMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(GlobalConstants.EdgeSchema.DIRECTION, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
            .addProperty(GlobalConstants.EdgeSchema.SOURCE, new Mappings.Mapping.Property()
                .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
            .addProperty(GlobalConstants.EdgeSchema.DEST, new Mappings.Mapping.Property()
                    .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                    .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }

    private static Iterable<Map<String, Object>> createOriginEdges(int numDragons, int numKingdoms) {
        List<Map<String, Object>> originEdges = new ArrayList<>();
        int counter = 0;
        for(int i = 0;i<numDragons;i++){
            Map<String, Object> originEdgeOut = new HashMap<>();
            originEdgeOut.put("id", ORIGINATED_IN.getName() + counter);
            originEdgeOut.put("type", ORIGINATED_IN.getName());
            originEdgeOut.put(GlobalConstants.EdgeSchema.DIRECTION, Direction.OUT.name());

            Map<String, Object> originEdgeIn = new HashMap<>();
            originEdgeIn.put("id", ORIGINATED_IN.getName() + counter+1);
            originEdgeIn.put("type", ORIGINATED_IN.getName());
            originEdgeIn.put(GlobalConstants.EdgeSchema.DIRECTION, Direction.IN.name());


            Map<String, Object> dragonEntity = new HashMap<>();
            dragonEntity.put("id", "Dragon_" + i);
            dragonEntity.put("type", DRAGON.name);

            Map<String, Object> kingdomEntity = new HashMap<>();
            kingdomEntity.put("id", "Kingdom_" + i % numKingdoms);
            kingdomEntity.put("type", KINGDOM.name);

            originEdgeOut.put(GlobalConstants.EdgeSchema.SOURCE, dragonEntity);
            originEdgeOut.put(GlobalConstants.EdgeSchema.DEST, kingdomEntity);

            originEdgeIn.put(GlobalConstants.EdgeSchema.SOURCE, kingdomEntity);
            originEdgeIn.put(GlobalConstants.EdgeSchema.DEST, dragonEntity);

            originEdges.add(originEdgeOut);
            originEdges.add(originEdgeIn);
            counter += 2;
        }

        return originEdges;
    }

    //endregion

    @Test
    public void testDragonOriginKingdomX2Path() throws IOException, InterruptedException, ParseException {
        Query query = getDragonOriginKingdomX2Query();

        CreateCsvCursorRequest request = CreateCsvCursorRequest.Builder.instance().withElement(new CreateCsvCursorRequest.CsvElement("A", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("B", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("C", "id", CreateCsvCursorRequest.ElementType.Entity)).request();
        runQueryAndValidate(query,dragonOriginKingdomX2Results(), request);
    }

    @Test
    public void testDragonOriginKingdomX2PathPlain() throws IOException, InterruptedException, ParseException {
        Query query = getDragonOriginKingdomX2Query();

        CreateCsvCursorRequest request = CreateCsvCursorRequest.Builder.instance().withElement(new CreateCsvCursorRequest.CsvElement("A", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("B", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("C", "id", CreateCsvCursorRequest.ElementType.Entity)).request();
        runQueryPlain(query,dragonOriginKingdomX2Results(), request);
    }

    @Test
    public void testDragonOriginKingdomX3Path() throws IOException, InterruptedException, ParseException {
        Query query = getDragonOriginKingdomX3Query();
        CreateCsvCursorRequest request = CreateCsvCursorRequest.Builder.instance().
                withElement(new CreateCsvCursorRequest.CsvElement("A", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("A", "name", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("A","B","rType", CreateCsvCursorRequest.ElementType.Rel))
                .withElement(new CreateCsvCursorRequest.CsvElement("B", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("C", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("D", "id", CreateCsvCursorRequest.ElementType.Entity))
                .withElement(new CreateCsvCursorRequest.CsvElement("G", "id", CreateCsvCursorRequest.ElementType.Entity)).request();
        runQueryAndValidate(query, dragonOriginKingdomX3Results(), request);
    }

    private CsvQueryResult dragonOriginKingdomX3Results() throws ParseException {
        CsvQueryResult.Builder builder = CsvQueryResult.Builder.instance();
        builder.withLine("\"Dragon_1\",\"B\",\"originatedIn\",\"Kingdom_1\",\"Dragon_3\",\"Dragon_5\",");
        builder.withLine("\"Dragon_1\",\"B\",\"originatedIn\",\"Kingdom_1\",\"Dragon_3\",\"Dragon_63\",");
        builder.withLine("\"Dragon_1\",\"B\",\"originatedIn\",\"Kingdom_1\",\"Dragon_61\",\"Dragon_5\",");
        builder.withLine("\"Dragon_1\",\"B\",\"originatedIn\",\"Kingdom_1\",\"Dragon_61\",\"Dragon_63\",");

        return builder.build();

    }

    private CsvQueryResult dragonOriginKingdomX2Results() throws ParseException {
        CsvQueryResult.Builder builder = CsvQueryResult.Builder.instance();
        builder.withLine("\"Dragon_1\",\"Kingdom_1\",\"Dragon_3\"");
        builder.withLine("\"Dragon_1\",\"Kingdom_1\",\"Dragon_61\"");
        return builder.build();

    }


    private void runQueryAndValidate(Query query, CsvQueryResult expectedQueryResult, CreateCsvCursorRequest csvCursorRequest) throws IOException, InterruptedException {
        csvCursorRequest.setCreatePageRequest(new CreatePageRequest(1000, true));
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(
                fuseResourceInfo.getQueryStoreUrl(),
                query,
                "1",
                "1",
                csvCursorRequest);

        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());

        Object pageData = queryResourceInfo.getCursorResourceInfos().iterator().next().getPageResourceInfos().iterator().next().getData();
        CsvQueryResult actualCsvQueryResult = new ObjectMapper().convertValue(pageData, CsvQueryResult.class);

        QueryResultAssert.assertEquals(expectedQueryResult, actualCsvQueryResult);
    }

    private void runQueryPlain(Query query, CsvQueryResult expectedQueryResult, CreateCsvCursorRequest csvCursorRequest) throws IOException, InterruptedException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), csvCursorRequest );
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        String res = fuseClient.getPageDataPlain(pageResourceInfo.getDataUrl());
        Assert.assertEquals(expectedQueryResult.content(), res);
    }

    private Query getDragonOriginKingdomX2Query() {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", $ont.eType$(DRAGON.name), "Dragon_1", "D0", 2, 0),
                new Rel(2, $ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(KINGDOM.name), 4, 0),
                new Rel(4, $ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 5, 0),
                new ETyped(5, "C", $ont.eType$(DRAGON.name), 6, 0),
                new EProp(6, NAME.type, Constraint.of(ConstraintOp.eq, "D"))
        )).build();
    }

    private Query getDragonOriginKingdomX3Query() {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", $ont.eType$(DRAGON.name), "Dragon_1", "D0", 2, 0),
                new Rel(2, $ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(KINGDOM.name), 4, 0),
                new Quant1(4, QuantType.all, Arrays.asList(5,8),0),
                new Rel(5, $ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 6, 0),
                new ETyped(6, "C", $ont.eType$(DRAGON.name), 7, 0),
                new EProp(7, NAME.type, Constraint.of(ConstraintOp.eq, "D")),
                new Rel(8, $ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 9, 0),
                new ETyped(9, "D", $ont.eType$(DRAGON.name), 10, 0),
                new EProp(10, NAME.type, Constraint.of(ConstraintOp.eq, "F"))
        )).build();
    }


    protected boolean shouldIgnoreRelId() {
        return true;
    }
    private static FuseClient fuseClient;
    private static Ontology.Accessor $ont;
    private static SimpleDateFormat sdf;

    private static Function<Long, Function<Long, Function<Integer, Long>>> timestampValueFunctionFactory;
    private static Function<Long, Function<Long, Function<Integer, Long>>> birthDateValueFunctionFactory;
    private static Function<Integer, Integer> temperatureValueFunction;
}
