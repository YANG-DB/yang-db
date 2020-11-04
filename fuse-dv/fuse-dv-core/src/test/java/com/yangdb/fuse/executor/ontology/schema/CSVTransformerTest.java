package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.load.CSVTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.DataTransformerContext;
import com.yangdb.fuse.executor.ontology.schema.load.DocumentBuilder;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.ontology.schema.IndexProviderRawSchema.getIndexPartitions;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class CSVTransformerTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static IndexProvider provider;
    private static Ontology ontology;
    private static RawSchema schema;
    private static Client client;
    private static Config config;
    private static OntologyProvider ontologyProvider;
    private static IndexProviderFactory providerIfc;


    @BeforeClass
    public static void setUp() throws Exception {
        client = Mockito.mock(Client.class);

        providerIfc = Mockito.mock(IndexProviderFactory.class);
        when(providerIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(provider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        InputStream providerStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderNested.conf");
        InputStream ontologyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");


        provider = mapper.readValue(providerStream, IndexProvider.class);
        ontology = mapper.readValue(ontologyStream, Ontology.class);

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProviderJsonFactory(config, providerIfc, ontologyProvider).get(ontology);

        schema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                return getIndexPartitions(schemaProvider,type);
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

    @Test
    public void testDragons() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Dragons.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Dragon";
            }

            public String type() {
                return "vertex";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(3, testTransform(transform).getEntities().size());
    }

    @Test
    public void testPerson() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Persons.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Person";
            }

            public String type() {
                return "vertex";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(3, testTransform(transform).getEntities().size());
    }

    @Test
    public void testGuild() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Guilds.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Guild";
            }

            public String type() {
                return "vertex";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(3, testTransform(transform).getEntities().size());
    }

    @Test
    public void testKingdom() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Kingdom.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Kingdom";
            }

            public String type() {
                return "vertex";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(4, testTransform(transform).getEntities().size());
    }

    @Test
    public void testHorse() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Horses.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Horse";
            }

            public String type() {
                return "vertex";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(3, testTransform(transform).getEntities().size());
    }

    @Test
    public void testFire() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Fire.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Fire";
            }

            public String type() {
                return "edge";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(4, testTransform(transform).getRelations().size());
    }

    @Test
    public void testFreeze() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Freeze.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Freeze";
            }

            public String type() {
                return "edge";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2, testTransform(transform).getRelations().size());
    }

    @Test
    public void testKnows() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Knows.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Know";
            }

            public String type() {
                return "edge";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(6, testTransform(transform).getRelations().size());
    }

    @Test
    public void testMemberOf() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/MemberOf.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "MemberOf";
            }

            public String type() {
                return "edge";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(6, testTransform(transform).getRelations().size());
    }

    @Test
    public void testOwns() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/Owns.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Own";
            }

            public String type() {
                return "edge";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(8, testTransform(transform).getRelations().size());
    }

    @Test
    public void testRegisteredIn() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/RegisteredIn.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "RegisteredIn";
            }

            public String type() {
                return "edge";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(14, testTransform(transform).getRelations().size());
    }

    @Test
    public void testSubjectIOf() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerIfc, schema, idGeneratorDriver, client);
        BufferedReader reader = Files.newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("schema/csv/SubjectOf.csv").getPath()));
        DataTransformerContext<Object> transform = transformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "SubjectOf";
            }

            public String type() {
                return "edge";
            }

            @Override
            public Reader content() {
                return reader;
            }
        }, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(6, testTransform(transform).getRelations().size());
    }

    public DataTransformerContext testTransform(DataTransformerContext<Object> transform) throws IOException {

        Assert.assertNotNull(transform);
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

        transform.getRelations()
                .stream()
                .map(DocumentBuilder::getNode)
                .forEach(r -> {
                    Assert.assertNotNull(r.get("type"));
                    Assert.assertNotNull(r.get("direction"));
                    Assert.assertNotNull(r.get("id"));

                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE));
                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("id"));
                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("type"));

                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST));
                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("id"));
                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("type"));

                    //"fire" "freeze" "own", "know", "memberOf", "originatedIn", "subjectOf", "registeredIn"
                    switch (r.get("type").asText()) {
                        case "Fire":
                            //check all fields exist
                            //         "date",
                            //        "temperature"

                            Assert.assertNotNull(r.get("date"));
                            Assert.assertNotNull(r.get("temperature"));

                            //side A redundant
                            Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("name"));
                            Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("color"));

                            //side B redundant
                            Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("name"));
                            Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("color"));
                            break;
                        case "Freeze":
                            //check all fields exist
                            //         "date",
                            //        "temperature"

                            Assert.assertNotNull(r.get("date"));
                            Assert.assertNotNull(r.get("temperature"));

                            //side A redundant
                            Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("name"));

                            //side B redundant
                            Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText(), "Dragon");
                            Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("name"));
                            break;
                        case "Own":
                            //         "startDate",
                            //        "endDate"
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            switch (r.get("direction").asText()) {
                                case "out":
                                    Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText(), "Person");
                                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("name"));
                                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("firstName"));

                                    //side B redundant
                                    Assert.assertTrue(r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText().equals("Horse") ||
                                            r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText().equals("Dragon"));
                                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("name"));
                                    break;
                                case "in":
                                    Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText(), "Person");
                                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("name"));
                                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.DEST).get("firstName"));

                                    //side B redundant
                                    Assert.assertTrue(r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText().equals("Horse") ||
                                            r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText().equals("Dragon"));
                                    Assert.assertNotNull(r.get(GlobalConstants.EdgeSchema.SOURCE).get("name"));
                                    break;
                            }
                            break;
                        case "Know":
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            //side A redundant
                            Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText(), "Person");

                            //side B redundant
                            Assert.assertEquals("Person", r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText());
                            break;
                        case "MemberOf":
                            //         "startDate",
                            //         "endDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            switch (r.get("direction").asText()) {
                                case "out":
                                    //side A redundant
                                    Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText(), "Person");

                                    //side B redundant
                                    Assert.assertEquals("Guild", r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText());
                                    break;
                                case "in":
                                    //side A redundant
                                    Assert.assertEquals(r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText(), "Person");

                                    //side B redundant
                                    Assert.assertEquals("Guild", r.get(GlobalConstants.EdgeSchema.SOURCE).get("type").asText());
                                    break;
                            }
                            break;
                        case "OriginatedIn":
                            //check all fields exist
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            switch (r.get("direction").asText()) {
                                case "out":
                                    //side A redundant
                                    Assert.assertTrue(r.get("entityA").get("type").asText().equals("Dragon") ||
                                            r.get("entityA").get("type").asText().equals("Person") ||
                                            r.get("entityA").get("type").asText().equals("Horse"));

                                    //side B redundant
                                    Assert.assertEquals("Kingdom", r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText());
                                    break;
                                case "in":
                                    //side A redundant
                                    Assert.assertTrue(r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText().equals("Dragon") ||
                                            r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText().equals("Person") ||
                                            r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText().equals("Horse"));

                                    //side B redundant
                                    Assert.assertEquals("Kingdom", r.get("entityA").get("type").asText());
                                    break;
                            }
                            break;
                        case "SubjectOf":
                            //check all fields exist
                            //check all fields exist
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));

                            switch (r.get("direction").asText()) {
                                case "out":
                                    //side A redundant
                                    Assert.assertEquals("Person", r.get("entityA").get("type").asText());

                                    //side B redundant
                                    Assert.assertEquals("Kingdom", r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText());
                                    break;
                                case "in":
                                    //side A redundant
                                    Assert.assertEquals("Person", r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText());

                                    //side B redundant
                                    Assert.assertEquals("Kingdom", r.get("entityA").get("type").asText());
                                    break;
                            }
                            break;
                        case "RegisteredIn":
                            //check all fields exist
                            //check all fields exist
                            //         "startDate",
                            //check all fields exist

                            Assert.assertNotNull(r.get("startDate"));
                            switch (r.get("direction").asText()) {
                                case "out":
                                    //side A redundant
                                    Assert.assertTrue(r.get("entityA").get("type").asText().equals("Guild") ||
                                            r.get("entityA").get("type").asText().equals("Horse"));

                                    //side B redundant
                                    Assert.assertEquals("Kingdom", r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText());
                                    break;
                                case "in":
                                    //side A redundant
                                    Assert.assertTrue(r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText().equals("Guild") ||
                                            r.get(GlobalConstants.EdgeSchema.DEST).get("type").asText().equals("Horse"));

                                    //side B redundant
                                    Assert.assertEquals("Kingdom", r.get("entityA").get("type").asText());
                                    break;
                            }
                            break;
                        default:
                            Assert.fail("Not expecting non registered type " + r.get("type").toString());
                    }
                });

        return transform;
    }
}
