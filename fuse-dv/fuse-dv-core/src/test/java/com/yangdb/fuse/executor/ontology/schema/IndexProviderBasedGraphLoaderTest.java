package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.load.EntityTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.IndexProviderBasedGraphLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class IndexProviderBasedGraphLoaderTest {
    public static final String ES_TEST = "es-test";
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static Client client;

    private static ObjectMapper mapper = new ObjectMapper();
    private static IndexProvider provider;
    private static Ontology ontology;
    private static RawSchema schema;


    private static void init(boolean embedded) throws Exception {
        // Start embedded ES
        if(embedded) {
            elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance(ES_TEST);
            client = elasticEmbeddedNode.getClient();
        } else {
            //use existing running ES
            client = elasticEmbeddedNode.getClient(ES_TEST, 9300);
        }

    }

    @BeforeClass
    public static void setUp() throws Exception {
        init(true);
        InputStream providerStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProvider.conf");
        InputStream ontologyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");

        provider = mapper.readValue(providerStream, IndexProvider.class);
        ontology = mapper.readValue(ontologyStream, Ontology.class);
        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProviderJsonFactory(provider, ontology).get(ontology);

        schema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                if(schemaProvider.getVertexSchemas(type).iterator().hasNext())
                    return schemaProvider.getVertexSchemas(type).iterator().next().getIndexPartitions().get();
                if(schemaProvider.getEdgeSchemas(type).iterator().hasNext())
                    return schemaProvider.getEdgeSchemas(type).iterator().next().getIndexPartitions().get();

                throw new FuseError.FuseErrorException("No valid partition found for " + type,new FuseError("IndexProvider Schema Error","No valid partition found for " + type));
            }

            @Override
            public String getIdFormat(String type) {
                return "";
            }

            @Override
            public String getPrefix(String type) {
                return "";
            }

            @Override
            public List<IndexPartitions.Partition> getPartitions(String type) {
                return StreamSupport.stream(getPartition(type).getPartitions().spliterator(), false)
                        .collect(Collectors.toList());

            }

            @Override
            public Iterable<String> indices() {
                Stream<String> edges = StreamSupport.stream(schemaProvider.getEdgeSchemas().spliterator(), false)
                        .flatMap(p -> StreamSupport.stream(p.getIndexPartitions().get().getPartitions().spliterator(), false))
                        .flatMap(v -> StreamSupport.stream(v.getIndices().spliterator(), false));
                Stream<String> vertices = StreamSupport.stream(schemaProvider.getVertexSchemas().spliterator(), false)
                        .flatMap(p -> StreamSupport.stream(p.getIndexPartitions().get().getPartitions().spliterator(), false))
                        .flatMap(v -> StreamSupport.stream(v.getIndices().spliterator(), false));

                return Stream.concat(edges,vertices)
                        .collect(Collectors.toSet());
            }
        };

    }


    @Test
    public void testSchema() throws IOException {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","guilds","own","subjectof","idx_freeze_1500","idx_fire_2000","people","idx_fire_1000","idx_freeze_500","kingdoms","know","originatedin","registeredin","memberof","horses","dragons").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(schema.indices().spliterator(),false).collect(Collectors.toSet()));
    }

    @Test
    public void testInit() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));
        EntityTransformer transformer = new EntityTransformer(ontology,provider,schema,idGeneratorDriver,client);
        IndexProviderBasedGraphLoader graphLoader = new IndexProviderBasedGraphLoader(client,ontology, transformer,schema, provider,idGeneratorDriver);
        Assert.assertEquals(19,graphLoader.init());
    }


    @Test
    public void testLoad() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        EntityTransformer transformer = new EntityTransformer(ontology,provider,schema,idGeneratorDriver,client);
        IndexProviderBasedGraphLoader graphLoader = new IndexProviderBasedGraphLoader(client,ontology, transformer,schema, provider,idGeneratorDriver);

        Assert.assertEquals(19,graphLoader.init());

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/LogicalDragonsGraph.json");
        LogicalGraphModel graphModel = mapper.readValue(stream, LogicalGraphModel.class);
        LoadResponse<String, FuseError> response = graphLoader.load(graphModel, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());

        Assert.assertEquals(0,response.getResponses().get(0).getFailures().size());
        Assert.assertEquals(0,response.getResponses().get(1).getFailures().size());

        String[] indices = StreamSupport.stream(schema.indices().spliterator(), false).collect(Collectors.toSet()).toArray(new String[]{});
        Assert.assertEquals(19,indices.length);

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices(indices);
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(graphModel.getNodes().size() + graphModel.getEdges().size(),resp.getHits().getTotalHits());

    }
}
