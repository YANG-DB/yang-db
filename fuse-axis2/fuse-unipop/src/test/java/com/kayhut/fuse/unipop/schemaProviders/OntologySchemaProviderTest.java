package com.kayhut.fuse.unipop.schemaProviders;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
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
        GraphVertexSchema vertexPersonSchema = ontologySchemaProvider.getVertexSchema("Person").get();

        assertEquals(vertexPersonSchema.getConstraint().getTraversalConstraint(), __.has(T.label, "Person"));
        List<String> indices = Stream.ofAll(vertexPersonSchema.getIndexPartitions().get().partitions()).flatMap(IndexPartitions.Partition::indices).toJavaList();
        assertEquals(2, indices.size());

        assertEquals("vertexIndex1", Stream.ofAll(indices).get(0));
        assertEquals("vertexIndex2", Stream.ofAll(indices).get(1));
    }

    @Test
    public void getEdgeSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphEdgeSchema edgeDragonFiresPersonSchema = ontologySchemaProvider.getEdgeSchema("Fire").get();
        assertEquals(edgeDragonFiresPersonSchema.getDestination().get().getLabel().get(), "Dragon");

        List<String> indices = Stream.ofAll(edgeDragonFiresPersonSchema.getIndexPartitions().get().partitions()).flatMap(IndexPartitions.Partition::indices).toJavaList();
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
        GraphVertexSchema person = ontologySchemaProvider.getVertexSchema("Person").get();
        GraphElementPropertySchema name = person.getProperty("name").get();
        Assert.assertEquals(name.getName(), "name");
    }

    //ednregion

    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, new OntologySchemaProvider.Adapter(
                Arrays.asList(
                        new GraphVertexSchema.Impl("Person", new StaticIndexPartitions(Arrays.asList("vertexIndex1", "vertexIndex2"))),
                        new GraphVertexSchema.Impl("Dragon", new StaticIndexPartitions(Arrays.asList("vertexIndex1", "vertexIndex2")))
                        ),
                Optional.empty(),
                Collections.emptyList(),
                Optional.of(new GraphEdgeSchema.Impl(
                        "",
                        Optional.of(new GraphEdgeSchema.End.Impl("entityA.id", Optional.of("Dragon"))),
                        Optional.of(new GraphEdgeSchema.End.Impl("entityB.id", Optional.of("Dragon"))),
                        Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
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