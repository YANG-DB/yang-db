package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderIfc;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.load.DataTransformerContext;
import com.yangdb.fuse.executor.ontology.schema.load.DocumentBuilder;
import com.yangdb.fuse.executor.ontology.schema.load.EntityTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class EntityTransformerTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static IndexProvider provider;
    private static Ontology ontology;
    private static RawSchema schema;
    private static Client client;
    private static Config config;
    private static OntologyProvider ontologyProvider;
    private static IndexProviderIfc providerIfc;


    @BeforeClass
    public static void setUp() throws Exception {
        client = Mockito.mock(Client.class);

        providerIfc = Mockito.mock(IndexProviderIfc.class);
        when(providerIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(provider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        InputStream providerStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProvider.conf");
        InputStream ontologyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");


        provider = mapper.readValue(providerStream, IndexProvider.class);
        ontology = mapper.readValue(ontologyStream, Ontology.class);

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

                return Stream.concat(edges, vertices)
                        .collect(Collectors.toSet());
            }
        };
    }


    @Test
    public void testTransform() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        EntityTransformer transformer = new EntityTransformer(ontology, provider, schema, idGeneratorDriver, client);
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/LogicalDragonsGraph.json");
        LogicalGraphModel graphModel = mapper.readValue(stream, LogicalGraphModel.class);
        DataTransformerContext transform = transformer.transform(graphModel, GraphDataLoader.Directive.INSERT);

        Assert.assertNotNull(transform);
        Assert.assertEquals(transform.getEntities().size(), graphModel.getNodes().size());
        transform.getEntities().stream().
                map(DocumentBuilder::getNode)
                .forEach(e -> {
                    Assert.assertNotNull(e.get("type"));
                    Assert.assertNotNull(e.get("id"));
                    //"people", "horses", "dragons","fire","freeze"
                    switch (e.get("type").asText()) {
                        case "Person":
                            //check all fields exist
                            //        "firstName",
                            //        "lastName",
                            //        "gender",
                            //        "birthDate",
                            //        "deathDate",
                            //        "height",
                            //        "name"
                            Assert.assertNotNull(e.get("firstName"));
                            Assert.assertNotNull(e.get("lastName"));
                            Assert.assertNotNull(e.get("gender"));
                            Assert.assertNotNull(e.get("birthDate"));
                            Assert.assertNotNull(e.get("deathDate"));
                            Assert.assertNotNull(e.get("height"));
                            Assert.assertNotNull(e.get("name"));
                            break;
                        case "Dragon":
                            //check all fields exist
                            //         "name",
                            //        "birthDate",
                            //        "power",
                            //        "gender",
                            //        "color"
                            Assert.assertNotNull(e.get("name"));
                            Assert.assertNotNull(e.get("birthDate"));
                            Assert.assertNotNull(e.get("power"));
                            Assert.assertNotNull(e.get("gender"));
                            Assert.assertNotNull(e.get("color"));
                            break;
                        case "Horse":
                            //check all fields exist
                            //        "name",
                            //        "weight",
                            //        "maxSpeed",
                            //        "distance"
                            Assert.assertNotNull(e.get("name"));
                            Assert.assertNotNull(e.get("weight"));
                            Assert.assertNotNull(e.get("maxSpeed"));
                            Assert.assertNotNull(e.get("distance"));
                            break;
                        case "Guild":
                            //check all fields exist
                            //        "name",
                            //        "description",
                            //        "iconId",
                            //        "url",
                            //        "establishDate"
                            Assert.assertNotNull(e.get("name"));
                            Assert.assertNotNull(e.get("description"));
                            Assert.assertNotNull(e.get("establishDate"));
                            break;
                        case "Kingdom":
                            //check all fields exist
                            //        "name",
                            //        "king",
                            //        "queen",
                            //        "independenceDay",
                            //        "funds"
                            Assert.assertNotNull(e.get("name"));
                            Assert.assertNotNull(e.get("king") != null ? e.get("king") : e.get("queen"));
                            Assert.assertNotNull(e.get("funds"));
                            Assert.assertNotNull(e.get("independenceDay"));
                            break;
                        default:
                            Assert.fail("Not expecting non registered type " + e.get("type").toString());
                    }
                });

        Assert.assertEquals(transform.getRelations().size(), graphModel.getEdges().size());
        transform.getRelations()
                .stream()
                .map(DocumentBuilder::getNode)
                .forEach(r -> {
                    Assert.assertNotNull(r.get("type"));
                    Assert.assertNotNull(r.get("id"));

                    Assert.assertNotNull(r.get("entityA"));
                    Assert.assertNotNull(r.get("entityA").get("id"));
                    Assert.assertNotNull(r.get("entityA").get("type"));

                    Assert.assertNotNull(r.get("entityB"));
                    Assert.assertNotNull(r.get("entityB").get("id"));
                    Assert.assertNotNull(r.get("entityB").get("type"));

                    //"fire" "freeze" "own", "know", "memberOf", "originatedIn", "subjectOf", "registeredIn"
                    switch (r.get("type").asText()) {
                        case "Fire":
                            //check all fields exist
                            //         "date",
                            //        "temperature"

                            Assert.assertNotNull(r.get("date"));
                            Assert.assertNotNull(r.get("temperature"));

                            //side A redundant
                            Assert.assertEquals(r.get("entityA").get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get("entityA").get("name"));
                            Assert.assertNotNull(r.get("entityA").get("color"));

                            //side B redundant
                            Assert.assertEquals(r.get("entityB").get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get("entityB").get("name"));
                            Assert.assertNotNull(r.get("entityB").get("color"));
                            break;
                        case "Freeze":
                            //check all fields exist
                            //         "date",
                            //        "temperature"

                            Assert.assertNotNull(r.get("date"));
                            Assert.assertNotNull(r.get("temperature"));

                            //side A redundant
                            Assert.assertEquals(r.get("entityA").get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get("entityA").get("name"));

                            //side B redundant
                            Assert.assertEquals(r.get("entityB").get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get("entityB").get("name"));
                            break;
                        case "Own":
                            //         "startDate",
                            //        "endDate"
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            Assert.assertEquals(r.get("entityA").get("type").asText(), "Person");
                            Assert.assertNotNull(r.get("entityA").get("name"));
                            Assert.assertNotNull(r.get("entityA").get("firstName"));

                            //side B redundant
                            Assert.assertTrue(r.get("entityB").get("type").asText().equals("Horse") ||
                                    r.get("entityB").get("type").asText().equals("Dragon"));
                            Assert.assertNotNull(r.get("entityB").get("name"));
                            break;
                        case "Know":
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            Assert.assertEquals(r.get("entityA").get("type").asText(), "Person");

                            //side B redundant
                            Assert.assertEquals("Person", r.get("entityB").get("type").asText());
                            break;
                        case "MemberOf":
                            //         "startDate",
                            //         "endDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            Assert.assertEquals(r.get("entityA").get("type").asText(), "Person");

                            //side B redundant
                            Assert.assertEquals("Guild", r.get("entityB").get("type").asText());
                            break;
                        case "OriginatedIn":
                            //check all fields exist
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            Assert.assertTrue(r.get("entityA").get("type").asText().equals("Dragon") ||
                                    r.get("entityA").get("type").asText().equals("Person") ||
                                    r.get("entityA").get("type").asText().equals("Horse"));

                            //side B redundant
                            Assert.assertEquals("Kingdom", r.get("entityB").get("type").asText());
                            break;
                        case "SubjectOf":
                            //check all fields exist
                            //check all fields exist
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            Assert.assertEquals("Person", r.get("entityA").get("type").asText());

                            //side B redundant
                            Assert.assertEquals("Kingdom", r.get("entityB").get("type").asText());
                            break;
                        case "RegisteredIn":
                            //check all fields exist
                            //check all fields exist
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            Assert.assertTrue(r.get("entityA").get("type").asText().equals("Guild") ||
                                    r.get("entityA").get("type").asText().equals("Horse"));

                            //side B redundant
                            Assert.assertEquals("Kingdom", r.get("entityB").get("type").asText());
                            break;
                        default:
                            Assert.fail("Not expecting non registered type " + r.get("type").toString());
                    }
                });


    }
}
