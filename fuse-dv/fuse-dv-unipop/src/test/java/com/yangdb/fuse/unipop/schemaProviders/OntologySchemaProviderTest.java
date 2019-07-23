package com.yangdb.fuse.unipop.schemaProviders;

import com.google.common.collect.Lists;
import com.yangdb.fuse.model.ontology.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
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
        GraphVertexSchema vertexPersonSchema = Stream.ofAll(ontologySchemaProvider.getVertexSchemas("Person")).get(0);

        assertEquals(vertexPersonSchema.getConstraint().getTraversalConstraint(), __.start().has(T.label, "Person"));
        List<String> indices = Stream.ofAll(vertexPersonSchema.getIndexPartitions().get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
        assertEquals(2, indices.size());

        assertEquals("vertexIndex1", Stream.ofAll(indices).get(0));
        assertEquals("vertexIndex2", Stream.ofAll(indices).get(1));
    }

    @Test
    public void getEdgeSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphEdgeSchema edgeDragonFiresPersonSchema = Stream.ofAll(ontologySchemaProvider.getEdgeSchemas("Fire")).get(0);
        assertEquals(edgeDragonFiresPersonSchema.getEndB().get().getLabel().get(), "Dragon");

        List<String> indices = Stream.ofAll(edgeDragonFiresPersonSchema.getIndexPartitions().get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
        assertEquals(2, indices.size());

        assertEquals("edgeIndex1", Stream.ofAll(indices).get(0));
        assertEquals("edgeIndex2", Stream.ofAll(indices).get(1));
    }

    @Test
    public void getEdgeSchemas() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        Iterable<GraphEdgeSchema> edgeSchemas = ontologySchemaProvider.getEdgeSchemas("Fire");
        assertEquals(Lists.newArrayList(edgeSchemas).size(), 1);
    }

    @Test
    public void vertexPropertiesTest(){
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphVertexSchema person = Stream.ofAll(ontologySchemaProvider.getVertexSchemas("Person")).get(0);
        GraphElementPropertySchema name = person.getProperty("name").get();
        Assert.assertEquals(name.getName(), "name");
    }

    //ednregion

    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl("Person", new StaticIndexPartitions(Arrays.asList("vertexIndex1", "vertexIndex2"))),
                        new GraphVertexSchema.Impl("Dragon", new StaticIndexPartitions(Arrays.asList("vertexIndex1", "vertexIndex2")))
                        ),
                Arrays.asList(
                    new GraphEdgeSchema.Impl(
                        "Fire",
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList("entityA.id"),
                                Optional.of("Dragon"))),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList("entityB.id"),
                                Optional.of("Dragon"))),
                        Direction.OUT,
                        Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                        new StaticIndexPartitions(Arrays.asList("edgeIndex1", "edgeIndex2"))))
        ));
    }

    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA("Dragon");
            seteTypeB("Dragon");
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