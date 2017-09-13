package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.unipop.controller.common.appender.IndexSearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.unipop.query.search.SearchQuery;

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

        ElementControllerContext context = new ElementControllerContext() {
            @Override
            public Optional<TraversalConstraint> getConstraint() {
                return Optional.of(traversalConstraint);
            }

            @Override
            public ElementType getElementType() {
                return ElementType.vertex;
            }

            @Override
            public GraphElementSchemaProvider getSchemaProvider() {
                return schemaProvider;
            }
        };

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

        ElementControllerContext context = new ElementControllerContext() {
            @Override
            public Optional<TraversalConstraint> getConstraint() {
                return Optional.of(traversalConstraint);
            }

            @Override
            public ElementType getElementType() {
                return ElementType.vertex;
            }

            @Override
            public GraphElementSchemaProvider getSchemaProvider() {
                return schemaProvider;
            }
        };

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
        //This test should return all the indices of the schema
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has("color","sheker"));

        ElementControllerContext context = new ElementControllerContext() {
            @Override
            public Optional<TraversalConstraint> getConstraint() {
                return Optional.of(traversalConstraint);
            }

            @Override
            public ElementType getElementType() {
                return ElementType.vertex;
            }

            @Override
            public GraphElementSchemaProvider getSchemaProvider() {
                return schemaProvider;
            }
        };

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
        return new OntologySchemaProvider(ontology, (label, elementType) -> {
            if (elementType == ElementType.vertex) {
                if (label.equals("Dragon")){
                    return () -> Arrays.<String>asList("dragonIndex1", "dragonIndex2");
                }
                else if(label.equals("Person")){
                    return () -> Arrays.<String>asList("personIndex1");
                }
                //Default
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