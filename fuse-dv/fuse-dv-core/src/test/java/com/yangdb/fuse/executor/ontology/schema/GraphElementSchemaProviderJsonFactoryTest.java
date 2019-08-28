package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GraphElementSchemaProviderJsonFactoryTest {
    private ObjectMapper mapper = new ObjectMapper();
    private IndexProvider provider;
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProvider.conf");
        provider = mapper.readValue(stream, IndexProvider.class);
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");
        ontology = mapper.readValue(stream, Ontology.class);
    }

    @Test
    public void testGraphElementSchemaProvider(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
    }

    @Test
    public void testGraphElementSchemaProviderLabel(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getEdgeLabels().spliterator(),false)
                .collect(Collectors.toSet()),new HashSet<>(Arrays.asList("Freez","Fire","Owns","SubjectOf","OriginatedIn","RegisteredIn","Knows","Member")));
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getVertexLabels().spliterator(),false)
                .collect(Collectors.toSet()),new HashSet<>(Arrays.asList("Horse","Guild","Person","Dragon","Kingdom")));
    }

    @Test
    public void testGraphElementSchemaProviderVertex(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(4, StreamSupport.stream(schemaProvider.getVertexSchemas().spliterator(), false)
                .filter(p->p.getIndexPartitions().isPresent())
                .filter(p->p.getIndexPartitions().get() instanceof StaticIndexPartitions)
                .filter(p->p.getIndexPartitions().get().getPartitions().iterator().hasNext())
                .count());
    }

    @Test
    public void testGraphElementSchemaProviderEdge(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(6, StreamSupport.stream(schemaProvider.getEdgeSchemas().spliterator(), false)
                .filter(p->p.getIndexPartitions().isPresent())
                .filter(p->p.getIndexPartitions().get() instanceof StaticIndexPartitions)
                .filter(p->p.getIndexPartitions().get().getPartitions().iterator().hasNext())
                .count());
    }
}
