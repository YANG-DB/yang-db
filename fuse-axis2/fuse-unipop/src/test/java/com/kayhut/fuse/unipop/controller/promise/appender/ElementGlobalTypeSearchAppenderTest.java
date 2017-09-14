package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.unipop.controller.common.appender.ElementGlobalTypeSearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.ConstraintContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 29-Mar-17.
 */
public class ElementGlobalTypeSearchAppenderTest {

    @Test
    public void testSimpleConstraint() throws JSONException {
        SearchBuilder searchBuilder = new SearchBuilder();
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        ElementGlobalTypeSearchAppender appender = new ElementGlobalTypeSearchAppender();

        ElementControllerContext context = new ElementControllerContext.Default(
                null,
                ElementType.vertex,
                schemaProvider,
                Optional.of(Constraint.by(__.has("name", "Sasson"))),
                Collections.emptyList(),
                0);

        boolean appendResult = appender.append(searchBuilder, context);

        Assert.assertTrue(appendResult);

        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
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