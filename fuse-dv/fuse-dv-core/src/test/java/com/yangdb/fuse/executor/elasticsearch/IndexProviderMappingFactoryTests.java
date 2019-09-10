package com.yangdb.fuse.executor.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.core.driver.BasicIdGenerator;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.executor.BaseModuleInjectionTest;
import com.yangdb.fuse.executor.ontology.schema.GraphElementSchemaProviderJsonFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.client.Client;
import org.junit.*;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Ignore
public class IndexProviderMappingFactoryTests extends BaseModuleInjectionTest {
    public static final String ES_TEST = "es-test";
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static Client client;
    private static RawSchema schema;
    private static ObjectMapper mapper = new ObjectMapper();
    private static IndexProvider provider;
    private static Ontology ontology;

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
                return schemaProvider.getVertexSchemas(type).iterator().next().getIndexPartitions().get();
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

    @AfterClass
    public static void tearDown() throws Exception {
        elasticEmbeddedNode.close();
    }

    @Test
    public void testGenerate() {
        IndexProviderMappingFactory mappingFactory = new IndexProviderMappingFactory(client, schema, ontology, provider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),11);
        List<String> indices = Arrays.asList("people", "horses", "dragons",
                "kingdoms", "guilds", "own", "know", "memberOf", "originatedIn", "subjectOf", "registeredIn");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toList()), indices);

        indices.forEach(index ->{
            switch (index){
                case "people":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names(index)).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index);
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("people"),"{\"people\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}");
            }
        });
    }

}
