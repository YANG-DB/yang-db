package com.yangdb.cyber.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.yangdb.cyber.ontology.schema.CyberIndexProviderBasedCSVLoaderIT;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.GraphElementSchemaProviderJsonFactory;
import com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.test.BaseSuiteMarker;
import org.elasticsearch.client.Client;
import org.jooby.Jooby;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema.getIndexPartitions;
import static com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode.FUSE_TEST_ELASTIC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CyberIndexProviderBasedCSVLoaderIT.class
})
public class CyberTestSuiteIndexProviderSuite implements BaseSuiteMarker {
    private static ElasticEmbeddedNode elasticEmbeddedNode;

    public static ObjectMapper mapper = new ObjectMapper();
    public static Config config;
    public static Ontology ontology;

    public static RawSchema schema;
    public static IndexProvider indexProvider;

    public static OntologyProvider ontologyProvider;
    public static IndexProviderFactory providerFactory;

    public static Client client;

    public static void setUpInternal() throws Exception {
        InputStream providerStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/CyberSchema.json");
        InputStream ontologyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Cyber.json");

        indexProvider = mapper.readValue(providerStream, IndexProvider.class);
        ontology = mapper.readValue(ontologyStream, Ontology.class);


        providerFactory = Mockito.mock(IndexProviderFactory.class);
        when(providerFactory.get(any())).thenAnswer(invocationOnMock -> Optional.of(indexProvider));


        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProviderJsonFactory(config, providerFactory, ontologyProvider).get(ontology);

        schema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                return getIndexPartitions(schemaProvider, type);
            }

            @Override
            public String getIdPrefix(String type) {
                return "";
            }

            @Override
            public String getIdFormat(String type) {
                return "";
            }

            @Override
            public String getIndexPrefix(String type) {
                return "";
            }

            @Override
            public List<IndexPartitions.Partition> getPartitions(String type) {
                return StreamSupport.stream(getPartition(type).getPartitions().spliterator(), false)
                        .collect(Collectors.toList());

            }

            @Override
            public Iterable<String> indices() {
                return IndexProviderRawSchema.indices(schemaProvider);
            }
        };

    }

    public static void setup() throws Exception {
        setup(true, FUSE_TEST_ELASTIC);
        setUpInternal();
    }

    public static void setup(boolean embedded) throws Exception {
        init(embedded, FUSE_TEST_ELASTIC);
        //init elasticsearch provider mapping factory
        setUpInternal();
    }

    public static void setup(boolean embedded, String name) throws Exception {
        init(embedded, name);
        //init elasticsearch provider mapping factory
        setUpInternal();
    }

    private static void init(boolean embedded, String name) throws Exception {
        // Start embedded ES
        if (embedded) {
            elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();
            client = ElasticEmbeddedNode.getClient(name);
        } else {
            //use existing running ES
            client = ElasticEmbeddedNode.getClient(name);
        }

    }

    public static void tearDown() throws Exception {
        if (elasticEmbeddedNode != null)
            elasticEmbeddedNode.close();
    }

    public static Client getClient() {
        return client;
    }

    //region Fields
    private static Jooby app;
    //endregion
}
