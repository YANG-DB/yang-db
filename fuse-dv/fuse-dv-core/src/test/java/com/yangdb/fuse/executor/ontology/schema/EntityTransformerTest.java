package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class EntityTransformerTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static IndexProvider provider;
    private static Ontology ontology;
    private static RawSchema schema;
    private static Client client;


    @BeforeClass
    public static void setUp() throws Exception {
        client = Mockito.mock(Client.class);

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



    @Test
    public void testTransform() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        EntityTransformer transformer = new EntityTransformer(ontology,provider,schema, idGeneratorDriver,client);
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/LogicalDragonsGraph.json");
        LogicalGraphModel graphModel = mapper.readValue(stream, LogicalGraphModel.class);
        DataTransformerContext transform = transformer.transform(graphModel, GraphDataLoader.Directive.INSERT);

        Assert.assertNotNull(transform);
        Assert.assertEquals(transform.getEntities().size(),graphModel.getNodes().size());
        transform.getEntities().forEach(e->{
            Assert.assertNotNull(e.get("type"));
            //"people", "horses", "dragons","fire","freeze"
            switch (e.get("type").asText()) {
                case "Person":
                    //check all fields exist
                    break;
                case "Dragon":
                    //check all fields exist
                    break;
                case "Horse":
                    //check all fields exist
                    break;
                case "Guild":
                    //check all fields exist
                    break;
                case "Kingdom":
                    //check all fields exist
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+e.get("type").toString());
            }
        });

        Assert.assertEquals(transform.getRelations().size(),graphModel.getEdges().size());
        transform.getRelations().forEach(r->{
            Assert.assertNotNull(r.get("type"));
            //"fire" "freeze" "own", "know", "memberOf", "originatedIn", "subjectOf", "registeredIn"
            switch (r.get("type").asText()) {
                case "Fire":
                    //check all fields exist
                    break;
                case "Freeze":
                    //check all fields exist
                    break;
                case "Own":
                    //check all fields exist
                    break;
                case "Know":
                    //check all fields exist
                    break;
                case "MemberOf":
                    //check all fields exist
                    break;
                case "OriginatedIn":
                    //check all fields exist
                    break;
                case "SubjectOf":
                    //check all fields exist
                    break;
                case "RegisteredIn":
                    //check all fields exist
                    break;
                default:
                    Assert.fail("Not expecting non registered type "+r.get("type").toString());
            }
        });



    }
}
