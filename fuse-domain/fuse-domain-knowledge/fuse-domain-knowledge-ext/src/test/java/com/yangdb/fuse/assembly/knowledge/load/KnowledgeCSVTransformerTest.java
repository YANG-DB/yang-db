package com.yangdb.fuse.assembly.knowledge.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.assembly.knowledge.KnowledgeRawSchemaShort;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.schema.load.CSVTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeLoaderUtils.*;
import static java.nio.file.Files.newBufferedReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class KnowledgeCSVTransformerTest {
    private ObjectMapper mapper = new ObjectMapper();
    private OntologyTransformer ontTransformer;
    private Ontology.Accessor knowledgeOnt;
    private OntologyProvider provider;

    @Before
    public void setUp() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("ontology/KnowledgeTransformation.json");
        URL knowledge = Thread.currentThread().getContextClassLoader().getResource("ontology/Knowledge.json");
        knowledgeOnt = new Ontology.Accessor(new ObjectMapper().readValue(knowledge, Ontology.class));
        ontTransformer = mapper.readValue(resource, OntologyTransformer.class);
        provider = new OntologyProvider() {
            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
            @Override
            public Optional<Ontology> get(String id) {
                switch (id) {
                    case "Knowledge":
                    default:
                        return Optional.of(knowledgeOnt.get());
                }
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(knowledgeOnt.get());
            }

        };

    }

    @Test
    public void transformEntity() {
        StoreAccessor client = Mockito.mock(StoreAccessor.class);
        when(client.findEntityById(anyString(), anyString(), anyString(), any()))
                .thenAnswer(invocationOnMock -> Optional.of(new HashMap()));

        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        final KnowledgeCSVTransformer csvTransformer = new KnowledgeCSVTransformer(provider,new KnowledgeRawSchemaShort(), ontTransformer, idGeneratorDriver, client);
        final KnowledgeContext transform = csvTransformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Person";
            }

            @Override
            public String type() {
                return "Entity";
            }

            @Override
            public Reader content() {
                try {
                    return newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("data/Person.csv").getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                throw new IllegalArgumentException();
            }

        }, GraphDataLoader.Directive.INSERT);
        assertNotNull(transform);
        assertEquals(3, transform.getEntities().size());
        assertEquals(9, transform.getEntities().get(0).additionalProperties.size());
        assertEquals(0, transform.getEntities().get(0).additional.size());
        assertEquals(0, transform.getEntities().get(0).hasRel.size());
        assertEquals(9, transform.getEntities().get(1).additionalProperties.size());
        assertEquals(0, transform.getEntities().get(1).additional.size());
        assertEquals(0, transform.getEntities().get(1).hasRel.size());
        assertEquals(9, transform.getEntities().get(2).additionalProperties.size());
        assertEquals(0, transform.getEntities().get(2).additional.size());
        assertEquals(0, transform.getEntities().get(2).hasRel.size());

        assertEquals(0, transform.getRelations().size());
        assertEquals(30, transform.geteValues().size());
        assertEquals(0, transform.getrValues().size());
    }

    @Test
    public void transformRelation() {
        StoreAccessor client = Mockito.mock(StoreAccessor.class);
        when(client.findEntityById(anyString(), anyString(), anyString(), any()))
                .thenAnswer(invocationOnMock ->{
                            HashMap map = new HashMap();
                            map.put(ID,"someId"+invocationOnMock.getArgument(1));
                            map.put(TECH_ID,"someTechId"+invocationOnMock.getArgument(1));
                            map.put(CATEGORY,"something"+invocationOnMock.getArgument(1));
                            return Optional.of(map);
                        }
                );

        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        final KnowledgeCSVTransformer csvTransformer = new KnowledgeCSVTransformer(provider,new KnowledgeRawSchemaShort(), ontTransformer, idGeneratorDriver, client);
        final KnowledgeContext transform = csvTransformer.transform(new CSVTransformer.CsvElement() {
            @Override
            public String label() {
                return "Knows";
            }

            @Override
            public String type() {
                return "Relation";
            }

            @Override
            public Reader content() {
                try {
                    return newBufferedReader(Paths.get(Thread.currentThread().getContextClassLoader().getResource("data/Knows.csv").getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                throw new IllegalArgumentException();
            }

        }, GraphDataLoader.Directive.INSERT);
        assertNotNull(transform);
        assertEquals(0, transform.getEntities().size());

        assertEquals(4, transform.getRelations().size());
        assertEquals(8, transform.getRelationBuilders().size());

        assertEquals(2, transform.getRelations().get(0).hasValues.size());
        assertEquals(8, transform.getRelations().get(0).additionalProperties.size());
        assertEquals(2, transform.getRelations().get(1).hasValues.size());
        assertEquals(8, transform.getRelations().get(1).additionalProperties.size());
        assertEquals(2, transform.getRelations().get(2).hasValues.size());
        assertEquals(8, transform.getRelations().get(2).additionalProperties.size());
        assertEquals(2, transform.getRelations().get(3).hasValues.size());
        assertEquals(8, transform.getRelations().get(3).additionalProperties.size());

        assertEquals(0, transform.geteValues().size());
        assertEquals(8, transform.getrValues().size());
    }
}