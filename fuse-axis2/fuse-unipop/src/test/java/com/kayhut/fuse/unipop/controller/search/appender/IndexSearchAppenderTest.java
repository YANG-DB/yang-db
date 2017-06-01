package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
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
        PromiseElementControllerContext promiseElementControllerContext = new
                PromiseElementControllerContext(
                    Collections.emptyList(),
                    Optional.of(traversalConstraint),
                    Collections.emptyList(),
                    schemaProvider,
                    ElementType.vertex,
                    mock(SearchQuery.class));

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder,promiseElementControllerContext);

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
        PromiseElementControllerContext promiseElementControllerContext = new
                PromiseElementControllerContext(
                    Collections.emptyList(),
                    Optional.of(traversalConstraint),
                    Collections.emptyList(),
                    schemaProvider,
                    ElementType.vertex,
                    mock(SearchQuery.class));

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder,promiseElementControllerContext);

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
        PromiseElementControllerContext promiseElementControllerContext = new
                PromiseElementControllerContext(
                    Collections.emptyList(),
                    Optional.of(traversalConstraint),
                    Collections.emptyList(),
                    schemaProvider,
                    ElementType.vertex,
                    mock(SearchQuery.class));

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder,promiseElementControllerContext);

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
            seteTypeA(2);
            seteTypeB(1);
        }});
        RelationshipType fireRelationshipType = RelationshipType.Builder.get()
                .withRType(1).withName("Fire").withEPairs(ePairs).build();
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person").build());
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(2).withName("Dragon").build());
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