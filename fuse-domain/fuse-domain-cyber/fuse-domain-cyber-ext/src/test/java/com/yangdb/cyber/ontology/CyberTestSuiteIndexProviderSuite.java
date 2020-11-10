package com.yangdb.cyber.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.yangdb.cyber.ontology.schema.CyberIndexProviderBasedCSVLoaderIT;
import com.yangdb.cyber.ontology.schema.CyberQueryIT;
import com.yangdb.cyber.ontology.schema.CyberSQLQueryIT;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.executor.ontology.schema.GraphElementSchemaProviderJsonFactory;
import com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.FuseUtils;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.test.BaseSuiteMarker;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema.getIndexPartitions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CyberIndexProviderBasedCSVLoaderIT.class,
        CyberQueryIT.class,
        CyberSQLQueryIT.class
})
public class CyberTestSuiteIndexProviderSuite implements BaseSuiteMarker {
    public static final String CYBER = "Cyber";

    public static Path path = Paths.get("src","resources","assembly", "Cyber","config", "application.test.engine3.m1.dfs.cyber.public.conf");
    public static String userDir = Paths.get("src",  "resources", "assembly", "Cyber").toFile().getAbsolutePath();


    public static ObjectMapper mapper = new ObjectMapper();
    public static Config config;
    public static Ontology ontology;

    public static RawSchema schema;
    public static IndexProvider indexProvider;

    public static OntologyProvider ontologyProvider;
    public static IndexProviderFactory providerFactory;

    public static Client client;
    public static FuseApp app = null;
    public static FuseClient fuseClient = null;

//    @AfterClass
    public static void after() {
//        Setup.cleanup();
        if (app != null) {
            app.stop();
        }
    }

    public static void loadSchema() throws Exception {
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

//    @BeforeClass
    public static void setup() throws Exception {
        setup(true, CYBER);
        loadSchema();
        startFuse(true);
    }

    public static void setup(boolean embedded) throws Exception {
        init(embedded, CYBER);
        //init elasticsearch provider mapping factory
        loadSchema();
    }

    public static void setup(boolean embedded, String name) throws Exception {
        init(embedded, name);
        //init elasticsearch provider mapping factory
        loadSchema();
    }

    private static void init(boolean embedded, String name) throws Exception {
        //set location aware user directory
        System.setProperty("user.dir", userDir);
        // Start embedded ES
        if (embedded) {
            GlobalElasticEmbeddedNode.getInstance(name);
            client = ElasticEmbeddedNode.getClient(name);
        } else {
            //use existing running ES
            client = ElasticEmbeddedNode.getClient(name);
        }
    }

    public static FuseClient getFuseClient() throws IOException {
        if (fuseClient == null)
            fuseClient = new BaseFuseClient("http://localhost:8888/fuse");
        return fuseClient;
    }


    public static void tearDown() throws Exception {
        GlobalElasticEmbeddedNode.close();
    }

    public static Client getClient() {
        return client;
    }

    public static void startFuse(boolean startFuse) {
        // Start fuse app (based on Jooby app web server)
        if (startFuse) {
            // Load fuse engine config file
            String confFilePath = path.toString();
            //load configuration
            Config config = FuseUtils.loadConfig(new File(confFilePath), "activeProfile");
            String[] joobyArgs = new String[]{
                    "logback.configurationFile=" + Paths.get("src", "test", "resources", "config", "logback.xml").toString(),
                    "server.join=false"
            };

            app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                    .conf(path.toFile(), "activeProfile");
            app.start("server.join=false");
        }
    }

}
