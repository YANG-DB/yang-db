package com.kayhut.fuse.unipop;

import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.appender.*;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 30-Mar-17.
 */
public class CompositeAllAppendersTest {

    @Test
    public void testSimpleCompositeAppender() {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has("label","Person"));

        PromiseElementControllerContext context = new
                PromiseElementControllerContext(Collections.emptyList(), Optional.of(traversalConstraint),schemaProvider,ElementType.vertex);

        SearchBuilder searchBuilder = new SearchBuilder();

        //Index Appender
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        //Global Appender
        ElementGlobalTypeSearchAppender globalAppender = new ElementGlobalTypeSearchAppender();
        //Element Constraint Search Appender
        ElementConstraintSearchAppender constraintSearchAppender = new ElementConstraintSearchAppender();

        //Testing the composition of the the above appenders
        CompositeSearchAppender compositeSearchAppender = new CompositeSearchAppender(CompositeSearchAppender.Mode.all, globalAppender);

        //Just Global Appender - nothing should be done - since the traversal contains a "Label"
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertFalse(appendResult);

        //Just Global Appender - nothing should be done beside the index appender
        compositeSearchAppender = new CompositeSearchAppender(CompositeSearchAppender.Mode.all, globalAppender, indexSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 1);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));

        // Index appender, Global Appender, Constraint Appender
        compositeSearchAppender = new CompositeSearchAppender(CompositeSearchAppender.Mode.all, globalAppender, indexSearchAppender, constraintSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 1);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"term\":{\"label\":\"Person\"}}}}}}",
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
