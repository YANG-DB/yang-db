package com.yangdb.fuse.unipop.controller.promise.appender;

import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.unipop.controller.common.appender.ElementGlobalTypeSearchAppender;
import com.yangdb.fuse.unipop.controller.common.context.ElementControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.yangdb.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.query.StepDescriptor;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 29-Mar-17.
 */
public class ElementGlobalTypeSearchAppenderTest {

    @Test
    @Ignore
    // This appender is deprecated
    public void testSimpleConstraint() throws JSONException {
        SearchBuilder searchBuilder = new SearchBuilder();
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        ElementGlobalTypeSearchAppender appender = new ElementGlobalTypeSearchAppender();

        ElementControllerContext context = new ElementControllerContext.Impl(
                null,
                new StepDescriptor(mock(Step.class)),
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
        return new OntologySchemaProvider(ontology, new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl("Dragon", new StaticIndexPartitions(Arrays.asList("dragonIndex1", "dragonIndex2"))),
                        new GraphVertexSchema.Impl("Person", new StaticIndexPartitions(Collections.singletonList("personIndex1"))),
                        new GraphVertexSchema.Impl("", new StaticIndexPartitions(Arrays.asList("vertexIndex1", "vertexIndex2")))
                ),
                Collections.singletonList(
                    new GraphEdgeSchema.Impl("", new StaticIndexPartitions(Arrays.asList("edgeIndex1", "edgeIndex2"))))
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