package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.query.search.SearchQuery;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 29-Mar-17.
 */
public class ElementGlobalTypeSearchAppenderTest {

    @Test
    public void testSimpleConstraint() {
        SearchBuilder searchBuilder = new SearchBuilder();
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        ElementGlobalTypeSearchAppender appender = new ElementGlobalTypeSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                Collections.emptyList(),
                Optional.of(Constraint.by(__.has("name", "Sasson"))),
                schemaProvider,
                ElementType.vertex,
                mock(SearchQuery.class)));

        Assert.assertTrue(appendResult);

        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
    }


    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider((label, elementType) -> {
            if (elementType == ElementType.vertex) {
                if (label.equals("Dragon")){
                    return Collections.singletonList(() -> Arrays.asList("dragonIndex1", "dragonIndex2"));
                }
                else if(label.equals("Person")){
                    return Collections.singletonList(() -> Arrays.asList("personIndex1"));
                }
                //Default
                return Collections.singletonList(() -> Arrays.asList("vertexIndex1", "vertexIndex2"));
            } else if (elementType == ElementType.edge) {
                return Collections.singletonList(() -> Arrays.asList("edgeIndex1", "edgeIndex2"));
            } else {
                // must fail
                Assert.assertTrue(false);
                return null;
            }
        }, ontology);
    }

    private Ontology getOntology() {
        Ontology ontology = mock(Ontology.class);
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