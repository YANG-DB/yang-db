package com.yangdb.fuse.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderIfc;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactoryIT;
import com.yangdb.fuse.executor.ontology.schema.*;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.test.BaseSuiteMarker;
import org.elasticsearch.client.Client;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        IndexProviderBasedGraphLoaderIT.class,
        IndexProviderBasedCSVLoaderIT.class,
        ElasticIndexProviderMappingFactoryIT.class,
        GraphInitiatorIT.class
})
public class TestSuiteIndexProviderSuite implements BaseSuiteMarker {
    private static ElasticEmbeddedNode elasticEmbeddedNode;

    public static ObjectMapper mapper = new ObjectMapper();
    public static Config config;
    public static Ontology ontology;

    public static RawSchema nestedSchema,embeddedSchema;
    public static IndexProvider nestedProvider,embeddedProvider;

    public static OntologyProvider ontologyProvider;
    public static IndexProviderIfc nestedProviderIfc,embeddedProviderIfc;

    public static Client client;

    public static void setUpInternal() throws Exception {
        client = ElasticEmbeddedNode.getClient();
        InputStream providerNestedStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderNested.conf");
        InputStream providerEmbeddedStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderEmbedded.conf");
        InputStream ontologyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");

        nestedProvider = mapper.readValue(providerNestedStream, IndexProvider.class);
        embeddedProvider = mapper.readValue(providerEmbeddedStream, IndexProvider.class);
        ontology = mapper.readValue(ontologyStream, Ontology.class);


        nestedProviderIfc = Mockito.mock(IndexProviderIfc.class);
        when(nestedProviderIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(nestedProvider));

        embeddedProviderIfc = Mockito.mock(IndexProviderIfc.class);
        when(embeddedProviderIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(embeddedProvider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        GraphElementSchemaProvider nestedSchemaProvider = new GraphElementSchemaProviderJsonFactory(config, nestedProviderIfc,ontologyProvider).get(ontology);
        GraphElementSchemaProvider embeddedSchemaProvider = new GraphElementSchemaProviderJsonFactory(config, embeddedProviderIfc,ontologyProvider).get(ontology);

        nestedSchema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                return nestedSchemaProvider.getVertexSchemas(type).iterator().next().getIndexPartitions().get();
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
                return IndexProviderRawSchema.indices(nestedSchemaProvider);
            }
        };

        embeddedSchema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                return embeddedSchemaProvider.getVertexSchemas(type).iterator().next().getIndexPartitions().get();
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
                return IndexProviderRawSchema.indices(embeddedSchemaProvider);
            }
        };
    }

    @BeforeClass
    public static void setup() throws Exception {
        init(true);
        setUpInternal();
    }

    private static void init(boolean embedded) throws Exception {
        // Start embedded ES
        if(embedded) {
            elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();
            client = elasticEmbeddedNode.getClient();
        } else {
            //use existing running ES
            client = elasticEmbeddedNode.getClient();
        }

    }

    @AfterClass
    public static void tearDown() throws Exception {
        if(elasticEmbeddedNode!=null)
            elasticEmbeddedNode.close();

    }



    public static Client getClient() {
        return client;
    }

    //region Fields
    private static Jooby app;
    //endregion
}
