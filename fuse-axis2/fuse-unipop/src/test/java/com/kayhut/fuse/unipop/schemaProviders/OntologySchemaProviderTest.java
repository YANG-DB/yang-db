package com.kayhut.fuse.unipop.schemaProviders;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 22-Mar-17.
 */
public class OntologySchemaProviderTest {

    //region Tests
    @Test
    public void getVertexSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphVertexSchema vertexPersonSchema = ontologySchemaProvider.getVertexSchema("Person").get();

        assertEquals(vertexPersonSchema.getType(), "Person");
        assertEquals(2, Stream.ofAll(vertexPersonSchema.getIndexPartition().getIndices()).size());

        Iterable<String> indices = Stream.ofAll(vertexPersonSchema.getIndexPartition().getIndices());

        assertEquals("vertexIndex1", Stream.ofAll(indices).get(0));
        assertEquals("vertexIndex2", Stream.ofAll(indices).get(1));
    }

    @Test
    public void getEdgeSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphEdgeSchema edgeDragonFiresPersonSchema = ontologySchemaProvider.getEdgeSchema("Fire").get();
        assertEquals(edgeDragonFiresPersonSchema.getDestination().get().getType().get(), "Person");


        assertEquals(2, Stream.ofAll(edgeDragonFiresPersonSchema.getIndexPartition().getIndices()).size());

        Iterable<String> indices = Stream.ofAll(edgeDragonFiresPersonSchema.getIndexPartition().getIndices());

        assertEquals("edgeIndex1", Stream.ofAll(indices).get(0));
        assertEquals("edgeIndex2", Stream.ofAll(indices).get(1));
    }

    @Test
    public void getEdgeSchemas() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = ontologySchemaProvider.getEdgeSchemas("Fire");
        assertEquals(Lists.newArrayList(edgeSchemas.get()).size(), 1);
    }

    @Test
    public void vertexPropertiesTest(){
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphVertexSchema person = ontologySchemaProvider.getVertexSchema("Person").get();
        GraphElementPropertySchema name = person.getProperty("name").get();
        Assert.assertEquals(name.getName(), "name");
    }

    //ednregion

    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, (label, elementType) -> {
            if (elementType == ElementType.vertex) {
                return () -> Arrays.<String>asList("vertexIndex1", "vertexIndex2");
            } else if (elementType == ElementType.edge) {
                return () -> Arrays.<String>asList("edgeIndex1", "edgeIndex2");
            } else {
                // must fail
                Assert.assertTrue(false);
                return null;
            }
        });
    }

    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA("Dragon");
            seteTypeB("Person");
        }});

        RelationshipType fireRelationshipType = RelationshipType.Builder.get()
                .withRType("Fire").withName("Fire").withEPairs(ePairs).build();

        Property nameProp = new Property();
        nameProp.setName("name");
        nameProp.setpType("1");

        when(ontology.getProperties()).then(invocationOnMock -> Collections.singletonList(nameProp));

        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("Person").withName("Person").withProperties(Collections.singletonList(nameProp.getpType())).build());
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("Dragon").withName("Dragon").build());
                    return entityTypes;
                }
        );

        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(fireRelationshipType);
                    return relTypes;
                }
        );

        return ontology;
    }
    //endregion
}