package com.yangdb.fuse.executor.ontology.schema;

import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.load.CSVTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.IndexProviderBasedCSVLoader;
import com.yangdb.fuse.model.results.LoadResponse;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.test.BaseITMarker;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.admin.indices.refresh.RefreshResponse;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.MatchAllQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.promise.appender.PromiseEdgeIndexAppender;
import com.yangdb.fuse.unipop.controller.promise.context.PromiseVertexControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.query.StepDescriptor;
import com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class IndexProviderBasedCSVLoaderIT implements BaseITMarker {
    private IndexProvider provider;
    private static IndexProviderFactory providerFactory;

    @Before
    public void setUp() throws Exception {

        providerFactory = Mockito.mock(IndexProviderFactory.class);
        when(providerFactory.get(any())).thenAnswer(invocationOnMock -> Optional.of(provider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderNested.conf");
        provider = mapper.readValue(stream, IndexProvider.class);
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");
        ontology = mapper.readValue(stream, Ontology.class);

        // every test starts from empty indices: tests share one node, and INSERT refuses to overwrite existing ids
        DataLoaderUtils.init(client, nestedSchema);
    }

    @Test
    public void testSchema() throws IOException {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","own","subjectof","dragon","idx_freeze_1500","idx_fire_2000","kingdom","people","idx_fire_1000","horse","guild","idx_freeze_500","know","registeredin","originatedin","memberof").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(nestedSchema.indices().spliterator(),false).collect(Collectors.toSet()));
    }


    @Test
    public void testLoadDragon() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer, nestedSchema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource = Thread.currentThread().getContextClassLoader().getResource("schema/csv/Dragons.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Entity","Dragon",new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());
        Assert.assertEquals(3,response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("dragon");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(3,resp.getHits().getTotalHits().value);

    }
    @Test
    public void testLoadFire() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer,nestedSchema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource  = Thread.currentThread().getContextClassLoader().getResource("schema/csv/Fire.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Relation","Fire",new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());
        Assert.assertEquals(4,response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("idx_fire*");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(4,resp.getHits().getTotalHits().value);

    }

    /**
     * L4: each Fire row lands in the range bucket covering its year (values are inclusive upper bounds),
     * a row after the last bucket is a load failure, and a date-filtered query searches only matching buckets.
     */
    @Test
    public void testLoadFireRoutesToTimeBuckets() throws IOException {
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, nestedProviderIfc, nestedSchema, Mockito.mock(IdGeneratorDriver.class), client);
        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer, nestedSchema);
        String header = "id,entityA.id,entityA.type,entityA.name,entityA.color,entityB.id,entityB.type,entityB.name,entityB.color,date,temperature\n";
        String rows = header
                + "F300,D1,Dragon,a,red,D2,Dragon,b,red,6/15/300,100\n"
                + "F750,D1,Dragon,a,red,D2,Dragon,b,red,6/15/750,100\n"
                + "F1000,D1,Dragon,a,red,D2,Dragon,b,red,6/15/1000,100\n"
                + "F1250,D1,Dragon,a,red,D2,Dragon,b,red,6/15/1250,100\n"
                + "F1778,D1,Dragon,a,red,D2,Dragon,b,red,6/15/1778,100\n"
                + "F1950,D1,Dragon,a,red,D2,Dragon,b,red,6/15/1950,100\n"
                + "F2050,D1,Dragon,a,red,D2,Dragon,b,red,6/15/2050,100\n";

        LoadResponse<String, FuseError> response = csvLoader.load("Relation", "Fire", rows, GraphDataLoader.Directive.INSERT);
        // every row is written twice (out + in direction); the 2050 row fits no bucket and fails loudly
        Assert.assertEquals(12, response.getResponses().get(1).getSuccesses().size());
        Assert.assertEquals(2, response.getResponses().get(1).getFailures().size());

        client.admin().indices().refresh(new RefreshRequest("idx_fire*")).actionGet();
        Assert.assertEquals(2, count("idx_fire_500"));
        Assert.assertEquals(4, count("idx_fire_1000"));
        Assert.assertEquals(2, count("idx_fire_1500"));
        Assert.assertEquals(4, count("idx_fire_2000"));

        // query side: date >= 1200-01-01 must search only idx_fire_1500 (1001..1500) and idx_fire_2000
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1200, Calendar.JANUARY, 1);
        Set<String> selected = selectFireIndices(P.gte(calendar.getTime()));
        Assert.assertEquals(new HashSet<>(Arrays.asList("idx_fire_1500", "idx_fire_2000")), selected);
        SearchResponse inSelected = client.prepareSearch(selected.toArray(new String[0]))
                .setQuery(QueryBuilders.rangeQuery("date").gte("1200-01-01 00:00:00.000")).setSize(100).get();
        SearchResponse inAll = client.prepareSearch("idx_fire*")
                .setQuery(QueryBuilders.rangeQuery("date").gte("1200-01-01 00:00:00.000")).setSize(100).get();
        Assert.assertEquals(6, inAll.getHits().getTotalHits().value);
        Assert.assertEquals(inAll.getHits().getTotalHits().value, inSelected.getHits().getTotalHits().value);
    }

    /**
     * L4 end-to-end: the service loads through the injected RawSchema chain
     * (Cached -> PartitionFiltered -> Prefixed -> IndexProviderRawSchema, see ExecutorModule.bindRawSchema),
     * and the tutorial's Fire.csv carries negative epoch-millis dates. 1900s rows belong in idx_fire_2000.
     */
    @Test
    public void testTutorialFireRowsThroughServiceSchemaChain() throws IOException {
        RawSchema serviceSchema = serviceSchemaChain();
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, nestedProviderIfc, serviceSchema, Mockito.mock(IdGeneratorDriver.class), client);
        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer, serviceSchema);
        // first rows of docs/tutorial/sample/dragons/csv/Fire.csv, verbatim
        String rows = "\"id\",\"entityA.id\",\"entityA.type\",\"entityB.id\",\"entityB.type\",\"date\",\"temperature\"\n"
                + "\"0_3_0\",\"0\",\"Dragon\",\"3\",\"Dragon\",\"-1917478511823\",\"187\"\n"
                + "\"0_3_1\",\"0\",\"Dragon\",\"3\",\"Dragon\",\"-1134316575110\",\"333\"\n"
                + "\"0_3_3\",\"0\",\"Dragon\",\"3\",\"Dragon\",\"-1900890540445\",\"360\"\n";

        LoadResponse<String, FuseError> response = csvLoader.load("Relation", "Fire", rows, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(6, response.getResponses().get(1).getSuccesses().size());

        client.admin().indices().refresh(new RefreshRequest("idx_fire*")).actionGet();
        Assert.assertEquals(0, count("idx_fire_500"));
        Assert.assertEquals(0, count("idx_fire_1000"));
        Assert.assertEquals(0, count("idx_fire_1500"));
        Assert.assertEquals(6, count("idx_fire_2000"));
    }

    static RawSchema serviceSchemaChain() {
        GraphElementSchemaProvider provider = new GraphElementSchemaProviderJsonFactory(config, nestedProviderIfc, ontologyProvider).get(ontology);
        RawSchema prefixed = new PrefixedRawSchema(new IndexProviderRawSchema(provider), "");
        RawSchema filtered = new PartitionFilteredRawSchema(prefixed, () -> client);
        return new CachedRawSchema(Collections::emptyList, filtered);
    }

    private static long count(String index) {
        return client.prepareSearch(index).setQuery(new MatchAllQueryBuilder()).setSize(0).get().getHits().getTotalHits().value;
    }

    private static Set<String> selectFireIndices(P<?> predicate) {
        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(
                Collections.emptyList(),
                Collections.singletonList(new GraphEdgeSchema.Impl("Fire", nestedSchema.getPartition("Fire"))));
        TraversalConstraint constraint = new TraversalConstraint(__.and(__.has(T.label, "Fire"), __.has("date", predicate)));
        VertexControllerContext context = new PromiseVertexControllerContext(null, new StepDescriptor(Mockito.mock(Step.class)),
                schemaProvider, Optional.of(constraint), Collections.emptyList(), 0, Collections.emptyList());
        SearchBuilder searchBuilder = new SearchBuilder();
        new PromiseEdgeIndexAppender().append(searchBuilder, context);
        return new HashSet<>(searchBuilder.getIndices());
    }

    @Test
    public void testInsertDoesNotOverwriteButUpsertDoes() throws IOException {
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, nestedProviderIfc, nestedSchema, Mockito.mock(IdGeneratorDriver.class), client);
        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer, nestedSchema);
        String header = "id,name,description,iconId,url,establishDate\n";

        LoadResponse<String, FuseError> first = csvLoader.load("Entity", "Guild", header + "G-DUP-1,first,,,,30/6/1020\n", GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(1, first.getResponses().get(1).getSuccesses().size());

        LoadResponse<String, FuseError> insertAgain = csvLoader.load("Entity", "Guild", header + "G-DUP-1,second,,,,30/6/1020\n", GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(0, insertAgain.getResponses().get(1).getSuccesses().size());
        Assert.assertEquals(1, insertAgain.getResponses().get(1).getFailures().size());
        Assert.assertEquals("first", client.prepareGet("guild", null, "G-DUP-1").get().getSource().get("name"));

        LoadResponse<String, FuseError> upsert = csvLoader.load("Entity", "Guild", header + "G-DUP-1,third,,,,30/6/1020\n", GraphDataLoader.Directive.UPSERT);
        Assert.assertEquals(1, upsert.getResponses().get(1).getSuccesses().size());
        Assert.assertEquals("third", client.prepareGet("guild", null, "G-DUP-1").get().getSource().get("name"));
    }
}
