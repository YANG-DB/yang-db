package com.yangdb.cyber.ontology.sql;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.elasticsearch.ClientProvider;
import com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory;
import com.yangdb.fuse.executor.ontology.schema.GraphElementSchemaProviderJsonFactory;
import com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.executor.sql.ElasticsearchFuseClient;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.test.BaseITMarker;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema.getIndexPartitions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ElasticsearchFuseClientTest implements BaseITMarker {
    ObjectMapper mapper = new ObjectMapper();
    IndexProvider provider;
    Ontology ontology;
    RawSchema schema;
    Config config;
    OntologyProvider ontologyProvider;
    IndexProviderFactory providerIfc;
    ElasticIndexProviderMappingFactory providerMappingFactory;



    @Before
    public void setUp() throws Exception {
        providerIfc = Mockito.mock(IndexProviderFactory.class);
        when(providerIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(provider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        InputStream providerStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("indexProvider/DragonsIndexProviderNested.conf");
        InputStream ontologyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");


        provider = mapper.readValue(providerStream, IndexProvider.class);
        ontology = mapper.readValue(ontologyStream, Ontology.class);

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProviderJsonFactory(config, providerIfc, ontologyProvider).get(ontology);

        schema = Mockito.mock(RawSchema.class);
        when(schema.indices()).thenAnswer(invocation -> IndexProviderRawSchema.indices(schemaProvider));
        when(schema.getPartition(any())).thenAnswer(invocation -> getIndexPartitions(schemaProvider, invocation.getArgument(0)));
        when(schema.getPartitions(any())).thenAnswer(invocation -> StreamSupport.stream(schema.getPartition(invocation.getArgument(0))
                .getPartitions().spliterator(), false).collect(Collectors.toList()));

        providerMappingFactory = new ElasticIndexProviderMappingFactory(ConfigFactory.empty(), Mockito.mock(Client.class), schema, ontology, provider);
    }

    @Test
    public void getIndexMappings() {
        ElasticsearchFuseClient client = new ElasticsearchFuseClient(Mockito.mock(Client.class),ontology, schema, provider, providerMappingFactory);
        Map<String, IndexMapping> mappings = client.getIndexMappings("Person");
        Assert.assertNotNull(mappings.get("people"));
        Assert.assertEquals(mappings.get("people").size(),9);
        Assert.assertEquals(mappings.get("people").getFieldType("name"),"text");
        Assert.assertEquals(mappings.get("people").getFieldType("birthDate"),"date");
        Assert.assertEquals(mappings.get("people").getFieldType("height"),"integer");
    }

    @Test
    public void searchSimpleSelectAllQuery() {

    }

}