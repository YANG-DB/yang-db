package com.yangdb.fuse.executor.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderIfc;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
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
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.client.Client;
import org.junit.*;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//todo "add TestSuite for both [IndexProviderBasedGraphLoaderTest,ElasticIndexProviderMappingFactoryTests]"
@Ignore("add TestSuite for both [IndexProviderBasedGraphLoaderTest,ElasticIndexProviderMappingFactoryTests]")
public class ElasticIndexProviderMappingFactoryTests extends BaseModuleInjectionTest {
    public static final String ES_TEST = "es-test";
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static Client client;
    private static RawSchema schema;
    private static ObjectMapper mapper = new ObjectMapper();
    private static IndexProvider provider;
    private static Ontology ontology;
    private static OntologyProvider ontologyProvider;
    private static IndexProviderIfc providerIfc;
    private static Config config;

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


        providerIfc = Mockito.mock(IndexProviderIfc.class);
        when(providerIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(provider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProviderJsonFactory(config, providerIfc,ontologyProvider).get(ontology);

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
    public void testGenerateMapping() {
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, provider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),13);
        List<String> indices = Arrays.asList("Person", "Horse", "Dragon","Fire","Freeze",
                "Kingdom", "Guild", "Own", "Know", "MemberOf", "OriginatedIn", "SubjectOf", "RegisteredIn");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), new HashSet<>(indices));

        indices.forEach(index ->{
            switch (index){
                case "people":
                case "Person":
                    GetIndexTemplatesResponse response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("people")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"people");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Person").toString(),"{\"Person\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"deathDate\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"height\":{\"type\":\"integer\"}}}}");
                    break;
                case "horses":
                case "Horse":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("horses")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"horses");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Horse").toString(),"{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"weight\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "dragons":
                case "Dragon":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("dragons")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"dragons");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Dragon").toString(),"{\"Dragon\":{\"properties\":{\"gender\":{\"type\":\"keyword\"},\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"},\"birthDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "kingdoms":
                case "Kingdom":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("kingdoms")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"kingdoms");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Kingdom").toString(),"{\"Kingdom\":{\"properties\":{\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "guilds":
                case "Guild":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("guilds")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"guilds");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Guild").toString(),"{\"Guild\":{\"properties\":{\"iconId\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"establishDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"url\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Own":
                case "own":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("own")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"own");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Own").toString(),"{\"Own\":{\"properties\":{\"entityA\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "Know":
                case "know":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("know")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),"know");
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Know").toString(),"{\"Know\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "memberof":
                case "MemberOf":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("memberof")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("MemberOf").toString(),"{\"MemberOf\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"endDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "originatedin":
                case "OriginatedIn":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("originatedin")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("OriginatedIn").toString(),"{\"OriginatedIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "subjectof":
                case "SubjectOf":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("subjectof")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("SubjectOf").toString(),"{\"SubjectOf\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "registeredin":
                case "RegisteredIn":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("registeredin")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("RegisteredIn").toString(),"{\"RegisteredIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"},\"startDate\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"}}}}");
                    break;
                case "Fire":
                case "fire":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("fire")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Fire").toString(),"{\"Fire\":{\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityA\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                case "Freeze":
                case "freeze":
                    response = client.admin().indices().getTemplates(new GetIndexTemplatesRequest().names("freeze")).actionGet();
                    Assert.assertEquals(response.getIndexTemplates().size(),1);
                    Assert.assertEquals(response.getIndexTemplates().get(0).name(),index.toLowerCase());
                    Assert.assertEquals(response.getIndexTemplates().get(0).settings().toString(),"{\"index.number_of_replicas\":\"1\",\"index.number_of_shards\":\"3\",\"index.sort.field\":\"id\",\"index.sort.order\":\"asc\"}");
                    Assert.assertEquals(response.getIndexTemplates().get(0).mappings().get("Freeze").toString(),"{\"Freeze\":{\"properties\":{\"date\":{\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\",\"type\":\"date\"},\"entityA\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"temperature\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type " + index);
            }
        });
    }

    @Test
    public void createIndicesTest() {
        ElasticIndexProviderMappingFactory mappingFactory = new ElasticIndexProviderMappingFactory(client, schema, ontology, provider);
        List<Tuple2<String, Boolean>> list = mappingFactory.generateMappings();
        Assert.assertEquals(list.size(),13);
        HashSet<String> labels = Sets.newHashSet("Person", "Horse", "Dragon", "Fire", "Freeze",
                "Kingdom", "Guild", "Own", "Know", "MemberOf", "OriginatedIn", "SubjectOf", "RegisteredIn");

        Assert.assertEquals(list.stream().map(i->i._1).collect(Collectors.toSet()), labels);

        Iterable<String> allIndices = schema.indices();
        javaslang.collection.Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());

        List<String> indices = mappingFactory.createIndices();
        List<String> names = Arrays.asList("own", "know", "memberof", "fire", "freeze", "originatedin","subjectof",
                "registeredin", "person", "horse", "dragon", "kingdom", "guild");

        Assert.assertEquals(indices, names);
        indices.forEach(index -> {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(index);
            GetMappingsResponse response = client.admin().indices().getMappings(request).actionGet();
            switch (index) {
                case "Own":
                case "own":
                    Assert.assertEquals(response.toString(),"{\"own\":{\"mappings\":{\"Own\":{\"properties\":{\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Know":
                case "know":
                    Assert.assertEquals(response.toString(),"{\"know\":{\"mappings\":{\"Know\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "memberOf":
                case "memberof":
                    Assert.assertEquals(response.toString(),"{\"memberof\":{\"mappings\":{\"MemberOf\":{\"properties\":{\"endDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Fire":
                case "fire":
                    Assert.assertEquals(response.toString(),"{\"fire\":{\"mappings\":{\"Fire\":{\"properties\":{\"date\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"color\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"temperature\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Freeze":
                case "freeze":
                    Assert.assertEquals(response.toString(),"{\"freeze\":{\"mappings\":{\"Freeze\":{\"properties\":{\"date\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"temperature\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "originatedIn":
                case "originatedin":
                    Assert.assertEquals(response.toString(),"{\"originatedin\":{\"mappings\":{\"OriginatedIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "subjectOf":
                case "subjectof":
                    Assert.assertEquals(response.toString(),"{\"subjectof\":{\"mappings\":{\"SubjectOf\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "registeredIn":
                case "registeredin":
                    Assert.assertEquals(response.toString(),"{\"registeredin\":{\"mappings\":{\"RegisteredIn\":{\"properties\":{\"entityA\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"entityB\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}},\"id\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Person":
                case "person":
                    Assert.assertEquals(response.toString(),"{\"person\":{\"mappings\":{\"Person\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"deathDate\":{\"type\":\"keyword\"},\"firstName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"gender\":{\"type\":\"keyword\"},\"height\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"lastName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Horse":
                case "horse":
                    Assert.assertEquals(response.toString(),"{\"horse\":{\"mappings\":{\"Horse\":{\"properties\":{\"distance\":{\"type\":\"integer\"},\"id\":{\"type\":\"keyword\"},\"maxSpeed\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"weight\":{\"type\":\"integer\"}}}}}}");
                    break;
                case "Dragon":
                case "dragon":
                    Assert.assertEquals(response.toString(),"{\"dragon\":{\"mappings\":{\"Dragon\":{\"properties\":{\"birthDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"color\":{\"type\":\"keyword\"},\"gender\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"power\":{\"type\":\"integer\"},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Kingdom":
                case "kingdom":
                    Assert.assertEquals(response.toString(),"{\"kingdom\":{\"mappings\":{\"Kingdom\":{\"properties\":{\"funds\":{\"type\":\"float\"},\"id\":{\"type\":\"keyword\"},\"independenceDay\":{\"type\":\"keyword\"},\"king\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"queen\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"}}}}}}");
                    break;
                case "Guild":
                case "guild":
                    Assert.assertEquals(response.toString(),"{\"guild\":{\"mappings\":{\"Guild\":{\"properties\":{\"description\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"establishDate\":{\"type\":\"date\",\"format\":\"epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS\"},\"iconId\":{\"type\":\"keyword\"},\"id\":{\"type\":\"keyword\"},\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"type\":{\"type\":\"keyword\"},\"url\":{\"type\":\"keyword\"}}}}}}");
                    break;
                default:
                    Assert.fail("Not expecting non registered type " + index);
            }
        });


    }
}
