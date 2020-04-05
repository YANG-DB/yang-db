package com.yangdb.fuse.dispatcher.query.graphql;

import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

public class GraphQLOntologySpaceXTranslatorTest {
    public static Ontology ontology;

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/spaceX.graphql");
        InputStream whereInoput = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/whereSchema.graphql");
        ontology = new GraphQL2OntologyTransformer().transform(resource,whereInoput);
        Assert.assertNotNull(ontology);
    }

    @Test
    public void testPropertiesTranslation() {
        Assert.assertEquals(ontology.getProperties().size(), 188);
    }

    @Test
    public void testEntitiesTranslation() {
        Assert.assertEquals(ontology.getEntityTypes().size(), 52);
    }

    @Test
    public void testRelationsTranslation() {
        Assert.assertEquals(ontology.getRelationshipTypes().size(), 43);
    }

}
