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
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.query.search.SearchQuery;

import java.util.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 30-Mar-17.
 */
public class CompositeAllAppendersTest {

    @Test
    public void testSimpleCompositeAppender() throws JSONException {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has(T.label, "Person"));
        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);

        PromiseElementControllerContext context = new
                PromiseElementControllerContext(
                    Collections.emptyList(),
                    Optional.of(traversalConstraint),
                    Collections.emptyList(),
                    schemaProvider,
                    ElementType.vertex,
                    searchQuery);

        SearchBuilder searchBuilder = new SearchBuilder();

        //Index Appender
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        //Global Appender
        ElementGlobalTypeSearchAppender globalAppender = new ElementGlobalTypeSearchAppender();
        //Element Constraint Search Appender
        ElementConstraintSearchAppender constraintSearchAppender = new ElementConstraintSearchAppender();

        //Testing the composition of the the above appenders
        CompositeSearchAppender<PromiseElementControllerContext> compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, globalAppender);

        //Just Global Appender - nothing should be done - since the traversal contains a "Label"
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertFalse(appendResult);

        //Just Global Appender - nothing should be done beside the index appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, globalAppender, indexSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 1);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));

        // Index appender, Global Appender, Constraint Appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, globalAppender, indexSearchAppender, constraintSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 1);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"term\":{\"_type\":\"Person\"}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

    }

    @Test
    public void testCompositeAppender_No_Label_AND_Statement() throws JSONException {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has("name", "bubu"), __.has("color", P.within((Collection)Arrays.asList("brown", "red")))));
        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);

        PromiseElementControllerContext context = new
                PromiseElementControllerContext(
                    Collections.emptyList(),
                    Optional.of(traversalConstraint),
                    Collections.emptyList(),
                    schemaProvider,
                    ElementType.vertex,
                    searchQuery);

        SearchBuilder searchBuilder = new SearchBuilder();

        //Index Appender
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        //Global Appender
        ElementGlobalTypeSearchAppender globalAppender = new ElementGlobalTypeSearchAppender();
        //Element Constraint Search Appender
        ElementConstraintSearchAppender constraintSearchAppender = new ElementConstraintSearchAppender();

        //Testing the composition of the the above appenders
        CompositeSearchAppender<PromiseElementControllerContext> compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, globalAppender);

        //One of the appenders should return true
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);

        //Since we didn't specify any Label in the constraint, we should get all vertex types.
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

        //Just Global Appender - nothing should be done beside the index appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, globalAppender, indexSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 3);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        Assert.assertTrue(searchBuilder.getIndices().contains("dragonIndex2"));

        //The query should be the same as above
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

        // Index appender, Global Appender, Constraint Appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, globalAppender, indexSearchAppender, constraintSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 3);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":[{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}},{\"bool\":{\"must\":[{\"term\":{\"name\":\"bubu\"}},{\"terms\":{\"color\":[\"brown\",\"red\"]}}]}}]}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

    }

    @Test
    public void testCompositeAppender_Label_OR_Statement() throws JSONException {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.or(__.has(T.label, "Dragon"), __.has("color", "yarok_bakbuk")));
        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);

        PromiseElementControllerContext context = new
                PromiseElementControllerContext(
                    Collections.emptyList(),
                    Optional.of(traversalConstraint),
                    Collections.emptyList(),
                    schemaProvider,
                    ElementType.vertex,
                    searchQuery);

        SearchBuilder searchBuilder = new SearchBuilder();

        //Index Appender
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        //Global Appender
        ElementGlobalTypeSearchAppender globalAppender = new ElementGlobalTypeSearchAppender();
        //Element Constraint Search Appender
        ElementConstraintSearchAppender constraintSearchAppender = new ElementConstraintSearchAppender();

        CompositeSearchAppender<PromiseElementControllerContext> compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, globalAppender);

        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);

        //Since we have a label, the global appender should be skip i.e., appender result is False
        Assert.assertFalse(appendResult);

        // Index appender, Constraint Appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, indexSearchAppender, constraintSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 2);
        HashSet<String> expectedIndicesSet = new HashSet<>(Arrays.asList("dragonIndex1", "dragonIndex2"));
        Assert.assertEquals(expectedIndicesSet, searchBuilder.getIndices());

        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"bool\":{\"should\":[{\"term\":{\"_type\":\"Dragon\"}},{\"term\":{\"color\":\"yarok_bakbuk\"}}]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

    }


    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, (label, elementType) -> {
            if (elementType == ElementType.vertex) {
                if (label.equals("Dragon")){
                    return () -> Arrays.asList("dragonIndex1", "dragonIndex2");
                }
                else if(label.equals("Person")){
                    return () -> Arrays.asList("personIndex1");
                }
                //Default
                return () -> Arrays.asList("vertexIndex1", "vertexIndex2");
            } else if (elementType == ElementType.edge) {
                return () -> Arrays.asList("edgeIndex1", "edgeIndex2");
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
