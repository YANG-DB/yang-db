package com.yangdb.fuse.model.graphql;

import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class GraphQLOntologyTranslatorTest {

    @Test
    public void testPropertiesTranslation() {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/StarWars.graphql");
        Ontology ontology = GraphQL2OntologyTransformer.transform(resource);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(ontology.getProperties().size(),4);
    }

    @Test
    public void testEnumsTranslation() {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/StarWars.graphql");
        Ontology ontology = GraphQL2OntologyTransformer.transform(resource);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(ontology.getEnumeratedTypes().size(),1);
    }
}
