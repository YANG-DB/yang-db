package com.kayhut.fuse.unipop.schemaProviders.helpers;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 22-Mar-17.
 */
public class OntologySchemaProviderTest {

    @Test
    public void getVertexSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = new OntologySchemaProvider("index", ontology );
        GraphVertexSchema vertexPersonSchema = ontologySchemaProvider.getVertexSchema("Person").get();
        assertEquals(vertexPersonSchema.getType(),"Person");
    }

    @Test
    public void getEdgeSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = new OntologySchemaProvider("index", ontology );
        GraphEdgeSchema edgeDragonFiresPersonSchema = ontologySchemaProvider.getEdgeSchema("Fire", Optional.of("Dragon"), Optional.of("Person")).get();
        assertEquals(edgeDragonFiresPersonSchema.getDestination().get().getType().get(),"Person");
    }

    @Test
    public void getEdgeSchemas() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = new OntologySchemaProvider("index", ontology );
        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = ontologySchemaProvider.getEdgeSchemas("Fire");
        assertEquals(Lists.newArrayList(edgeSchemas.get()).size(),1);
    }

    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA(2);
            seteTypeB(1);
        }});
        RelationshipType fireRelationshipType = RelationshipType.RelationshipTypeBuilder.aRelationshipType()
                .withRType(1).withName("Fire").withEPairs(ePairs).build();
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person").build());
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(2).withName("Dragon").build());
                    return  entityTypes;
                }
        );
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(fireRelationshipType);
                    return  relTypes;
                }
        );

        return ontology;
    }

}