package com.yangdb.fuse.unipop.controller.promise.appender;

import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.unipop.controller.common.appender.IndexSearchAppender;
import com.yangdb.fuse.unipop.controller.common.context.ElementControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.yangdb.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.unipop.query.StepDescriptor;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 29-Mar-17.
 */
public class IndexSearchAppenderTest {

    @Test
    public void appendTest_Constraint_labelDragon() throws Exception {
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has(T.label, "Dragon"));

        ElementControllerContext context = new ElementControllerContext.Impl(
                null,
                new StepDescriptor(mock(Step.class)),
                ElementType.vertex,
                schemaProvider,
                Optional.of(traversalConstraint),
                Collections.emptyList(),
                0
        );

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder, context);

        assertEquals(appendResult, true);
        assertEquals(2,searchBuilder.getIndices().size());
        assertTrue(searchBuilder.getIndices().contains("dragonIndex1"));
        assertTrue(searchBuilder.getIndices().contains("dragonIndex2"));
        assertFalse(searchBuilder.getIndices().contains("vertexIndex1"));
    }

    @Test
    public void appendTest_Constraint_labelPerson() throws Exception {
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has(T.label, "Person"));

        ElementControllerContext context = new ElementControllerContext.Impl(
                null,
                new StepDescriptor(mock(Step.class)),
                ElementType.vertex,
                schemaProvider,
                Optional.of(traversalConstraint),
                Collections.emptyList(),
                0
        );

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder, context);

        assertEquals(appendResult, true);
        assertEquals(1,searchBuilder.getIndices().size());
        assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        assertFalse(searchBuilder.getIndices().contains("vertexIndex1"));
    }

    @Test
    public void appendTest_Constraint_noLabel() throws Exception {
        //This test should return all the getIndices of the schema
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has("color","sheker"));

        ElementControllerContext context = new ElementControllerContext.Impl(
                null,
                new StepDescriptor(mock(Step.class)),
                ElementType.vertex,
                schemaProvider,
                Optional.of(traversalConstraint),
                Collections.emptyList(),
                0);

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder, context);

        assertEquals(appendResult, true);
        assertEquals(3,searchBuilder.getIndices().size());
        assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        assertFalse(searchBuilder.getIndices().contains("vertexIndex1"));
    }

    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl("Dragon", new StaticIndexPartitions(Arrays.asList("dragonIndex1", "dragonIndex2"))),
                        new GraphVertexSchema.Impl("Person", new StaticIndexPartitions(Collections.singletonList("personIndex1")))
                ),
                Collections.emptyList()
        ));
    }

    private Ontology getOntology() {
        Ontology ontology = mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA("Dragon");
            seteTypeB("Person");
        }});
        RelationshipType fireRelationshipType = RelationshipType.Builder.get()
                .withRType("Fire").withName("Fire").withEPairs(ePairs).build();
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("Person").withName("Person").build());
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